package org.yangxin.im.service.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.pack.message.MessageReadedPack;
import org.yangxin.im.common.enums.command.MessageCommand;
import org.yangxin.im.common.model.message.MessageReadedContent;
import org.yangxin.im.common.model.message.MessageReceiveAckContent;
import org.yangxin.im.service.util.MessageProducer;

@Service
@RequiredArgsConstructor
public class MessageSyncService {
    private final MessageProducer messageProducer;

    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {
        messageProducer.sendToUser(messageReceiveAckContent.getToId(), MessageCommand.MSG_RECEIVE_ACK,
                messageReceiveAckContent, messageReceiveAckContent.getAppId());
    }

    public void readMark(MessageReadedContent messageContent) {
        /*
            消息已读，更新会话的 seq ，通知在线的同步端发送指定 command ，发送已读回执通知
         */
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageContent, messageReadedPack);
        syncToSender(messageReadedPack, messageContent);
        // 发送给对方
        messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_READED_RECEIPT, messageReadedPack,
                messageContent.getAppId());
    }

    private void syncToSender(MessageReadedPack pack, MessageReadedContent content) {
        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getFromId(), MessageCommand.MSG_READED_NOTIFY,
                pack, content);
    }
}
