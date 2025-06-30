package org.yangxin.im.common.model.message;

import lombok.Getter;
import lombok.Setter;
import org.yangxin.im.common.model.ClientInfo;

@Getter
@Setter
public class MessageReadedContent extends ClientInfo {
    private long messageSequence;
    private String fromId;
    private String toId;
    private Integer conversationType;
}
