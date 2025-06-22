package org.yangxin.im.service.message.model;

import org.yangxin.im.common.model.ClientInfo;

public class MessageContent extends ClientInfo {
    private String messageId;
    private String fromId;
    private String toId;
    private String messageBody;
}
