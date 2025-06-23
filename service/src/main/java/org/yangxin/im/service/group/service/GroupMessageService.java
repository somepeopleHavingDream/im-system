package org.yangxin.im.service.group.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.pack.message.ChatMessageAck;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.enums.command.GroupEventCommand;
import org.yangxin.im.common.enums.command.MessageCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.message.GroupChatMessageContent;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.service.group.model.req.SendGroupMessageReq;
import org.yangxin.im.service.message.model.resp.SendMessageResp;
import org.yangxin.im.service.message.service.CheckSendMessageService;
import org.yangxin.im.service.message.service.MessageStoreService;
import org.yangxin.im.service.util.MessageProducer;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMessageService {
    private final CheckSendMessageService checkSendMessageService;
    private final MessageProducer messageProducer;
    private final ImGroupMemberService imGroupMemberService;
    private final MessageStoreService messageStoreService;

    public void process(GroupChatMessageContent messageContent) {
        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();

        // 前置校验
        // 这个用户是否被禁言、是否被禁用
        // 发送方和接收方是否是好友
        ResponseVO<?> responseVO = imServerPermissionCheck(fromId, groupId, appId);
        if (responseVO.isOk()) {
            messageStoreService.storeGroupMessage(messageContent);
            // 回 ack 给自己
            groupAck(messageContent, responseVO);
            // 发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 发消息给对方在线端
            dispatchMessage(messageContent);
        } else {
            // 告诉客户端失败了
            // ack
            groupAck(messageContent, responseVO);
        }
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
