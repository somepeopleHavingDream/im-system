package org.yangxin.im.common.model.message;

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
    private Long messageTime;
    private Long createTime;
    private String extra;
    private Long messageKey;
}
