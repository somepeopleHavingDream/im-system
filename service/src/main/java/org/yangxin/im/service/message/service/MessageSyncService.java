package org.yangxin.im.service.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yangxin.im.common.enums.command.MessageCommand;
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
}
