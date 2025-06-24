package org.yangxin.im.service.message.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.pack.message.ChatMessageAck;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.enums.command.MessageCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.service.message.model.req.SendMessageReq;
import org.yangxin.im.service.message.model.resp.SendMessageResp;
import org.yangxin.im.service.util.MessageProducer;

import javax.annotation.Resource;
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
        // 前置校验
        // 这个用户是否被禁言、是否被禁用
        // 发送方和接收方是否是好友
        threadPoolExecutor.execute(() -> {
            // 插入数据
            messageStoreService.storeP2PMessage(messageContent);
            // 回 ack 给自己
            ack(messageContent, ResponseVO.successResponse());
            // 发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 发消息给对方在线端
            dispatchMessage(messageContent);
        });
    }

    private void dispatchMessage(MessageContent messageContent) {
        messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P, messageContent,
                messageContent.getAppId());
    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        log.info("ack {} {}", messageContent.getMessageId(), responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        // 发消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK, responseVO, messageContent);
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
