package org.yangxin.im.service.group.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.pack.message.ChatMessageAck;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.command.GroupEventCommand;
import org.yangxin.im.common.enums.command.MessageCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.message.GroupChatMessageContent;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.service.group.model.req.SendGroupMessageReq;
import org.yangxin.im.service.message.model.resp.SendMessageResp;
import org.yangxin.im.service.message.service.CheckSendMessageService;
import org.yangxin.im.service.message.service.MessageStoreService;
import org.yangxin.im.service.seq.RedisSeq;
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
public class GroupMessageService {
    @Resource
    private CheckSendMessageService checkSendMessageService;
    @Resource
    private MessageProducer messageProducer;
    @Resource
    private ImGroupMemberService imGroupMemberService;
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
                    thread.setName("message-group-thread-" + num.getAndIncrement());
                    return thread;
                });
    }

    public void process(GroupChatMessageContent messageContent) {
        // 前置校验
        // 这个用户是否被禁言、是否被禁用
        // 发送方和接收方是否是好友
        Long seq =
                redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.GroupMessage + ":" + messageContent.getGroupId());
        messageContent.setMessageSequence(seq);
        threadPoolExecutor.execute(() -> {
            messageStoreService.storeGroupMessage(messageContent);
            // 回 ack 给自己
            groupAck(messageContent, ResponseVO.successResponse());
            // 发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 发消息给对方在线端
            dispatchMessage(messageContent);
        });
    }

    private void dispatchMessage(GroupChatMessageContent messageContent) {
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(),
                messageContent.getAppId());
        for (String memberId : groupMemberId) {
            if (!memberId.equals(messageContent.getFromId())) {
                messageProducer.sendToUser(memberId, GroupEventCommand.MSG_GROUP, messageContent,
                        messageContent.getAppId());
            }
        }
    }

    private void groupAck(MessageContent messageContent, ResponseVO responseVO) {
        log.info("groupAck {} {}", messageContent.getMessageId(), responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        // 发消息
        messageProducer.sendToUser(messageContent.getFromId(), GroupEventCommand.MSG_GROUP, responseVO, messageContent);
    }

    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent,
                messageContent);
    }

    private ResponseVO<?> imServerPermissionCheck(String fromId, String toId, Integer appId) {
        return checkSendMessageService.checkGroupMessage(fromId, toId, appId);
    }

    public Object send(SendGroupMessageReq req) {
        SendMessageResp sendMessageResp = new SendMessageResp();
        GroupChatMessageContent message = new GroupChatMessageContent();
        BeanUtils.copyProperties(req, message);

        messageStoreService.storeGroupMessage(message);

        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        // 发消息给同步在线端
        syncToSender(message, message);
        // 发消息给对方在线端
        dispatchMessage(message);

        return sendMessageResp;
    }
}
