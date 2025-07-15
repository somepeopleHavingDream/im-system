package org.yangxin.im.service.message.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.pack.message.ChatMessageAck;
import org.yangxin.im.codec.pack.message.MessageReceiveServerAckPack;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.ConversationTypeEnum;
import org.yangxin.im.common.enums.command.MessageCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.common.model.message.OfflineMessageContent;
import org.yangxin.im.service.message.model.req.SendMessageReq;
import org.yangxin.im.service.message.model.resp.SendMessageResp;
import org.yangxin.im.service.seq.RedisSeq;
import org.yangxin.im.service.util.ConversationIdGenerate;
import org.yangxin.im.service.util.MessageProducer;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"rawtypes", "unchecked"})
@Service
@Slf4j
public class P2PMessageService {
    @Resource
    private CheckSendMessageService checkSendMessageService;
    @Resource
    private MessageProducer messageProducer;
    @Resource
    private MessageStoreService messageStoreService;
    @Resource
    private RedisSeq redisSeq;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("message-process-thread-" + num.getAndIncrement());
                    return thread;
                });
    }

    public void process(MessageContent messageContent) {
        MessageContent messageFromMessageIdCache =
                messageStoreService.getMessageFromMessageIdCache(messageContent.getAppId(),
                        messageContent.getMessageId(), MessageContent.class);
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(() -> {
                // 回 ack 给自己
                ack(messageContent, ResponseVO.successResponse());
                // 发消息给同步在线端
                syncToSender(messageFromMessageIdCache, messageFromMessageIdCache);
                // 发消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageFromMessageIdCache);
                if (clientInfos.isEmpty()) {
                    // 发送接受确认给发送方，要带上是服务端发送的标识
                    receiveAck(messageContent);
                }
            });
            return;
        }

        long seq =
                redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.Message + ":" + ConversationIdGenerate.generateP2PId(messageContent.getFromId(), messageContent.getToId()));
        messageContent.setMessageSequence(seq);

        // 校验前置
        threadPoolExecutor.execute(() -> {
            // 插入数据
            messageStoreService.storeP2PMessage(messageContent);
            // 插入离线消息
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent, offlineMessageContent);
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            messageStoreService.storeOfflineMessage(offlineMessageContent);
            // 回 ack 给自己
            ack(messageContent, ResponseVO.successResponse());
            // 发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 发消息给对方在线端
            List<ClientInfo> clientInfos = dispatchMessage(messageContent);
            // 将 messageId 存到缓存中
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId()
                    , messageContent);
            if (clientInfos.isEmpty()) {
                // 发送接受确认给发送方，要带上是服务端发送的标识
                receiveAck(messageContent);
            }
        });
    }

    private List<ClientInfo> dispatchMessage(MessageContent messageContent) {
        return messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P, messageContent,
                messageContent.getAppId());
    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        log.info("ack {} {}", messageContent.getMessageId(), responseVO.getCode());
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId(),
                messageContent.getMessageSequence());
        responseVO.setData(chatMessageAck);
        // 发消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK, responseVO, messageContent);
    }

    public void receiveAck(MessageContent messageContent) {
        MessageReceiveServerAckPack pack = new MessageReceiveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);

        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_RECEIVE_ACK, pack,
                new ClientInfo(messageContent.getAppId(), messageContent.getClientType(), messageContent.getImei()));
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent,
                messageContent);
    }

    public ResponseVO<?> imServerPermissionCheck(String fromId, String toId, Integer appId) {
        ResponseVO<?> responseVO = checkSendMessageService.checkSenderForbiddenAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }
        return checkSendMessageService.checkFriendShip(fromId, toId, appId);
    }

    public SendMessageResp send(SendMessageReq req) {
        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req, message);
        // 插入数据
        messageStoreService.storeP2PMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        // 发消息给同步在线端
        syncToSender(message, message);
        // 发消息给对方在线端
        dispatchMessage(message);

        return sendMessageResp;
    }
}
