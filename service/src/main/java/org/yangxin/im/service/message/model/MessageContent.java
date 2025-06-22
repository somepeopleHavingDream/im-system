package org.yangxin.im.service.message.model;

import lombok.Getter;
import lombok.Setter;
import org.yangxin.im.common.model.ClientInfo;

@Getter
@Setter
public class MessageContent extends ClientInfo {
    private String messageId;
    private String fromId;
    private String toId;
    private String messageBody;
}
