package org.yangxin.im.service.message.service;

import lombok.RequiredArgsConstructor;
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

@SuppressWarnings({"rawtypes", "unchecked"})
@Service
@RequiredArgsConstructor
@Slf4j
public class P2PMessageService {
    private final CheckSendMessageService checkSendMessageService;
    private final MessageProducer messageProducer;
    private final MessageStoreService messageStoreService;

    public void process(MessageContent messageContent) {
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();

        // 前置校验
        // 这个用户是否被禁言、是否被禁用
        // 发送方和接收方是否是好友
        ResponseVO<?> responseVO = imServerPermissionCheck(fromId, toId, messageContent);
        if (responseVO.isOk()) {
            // 插入数据
            messageStoreService.storeP2PMessage(messageContent);
            // 回 ack 给自己
            ack(messageContent, responseVO);
            // 发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 发消息给对方在线端
            dispatchMessage(messageContent);
        } else {
            // 告诉客户端失败了
            // ack
            ack(messageContent, responseVO);
        }

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

    private ResponseVO<?> imServerPermissionCheck(String fromId, String toId, MessageContent messageContent) {
        ResponseVO<?> responseVO = checkSendMessageService.checkSenderForbiddenAndMute(fromId,
                messageContent.getAppId());
        if (!responseVO.isOk()) {
            return responseVO;
        }
        return checkSendMessageService.checkFriendShip(fromId, toId, messageContent.getAppId());
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
