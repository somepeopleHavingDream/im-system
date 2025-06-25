package org.yangxin.im.common.model.message;

import lombok.Getter;
import lombok.Setter;
import org.yangxin.im.common.model.ClientInfo;

@Getter
@Setter
public class MessageReceiveAckContent extends ClientInfo {
    private Long messageKey;
    private String fromId;
    private String toId;
    private Long messageSequence;
}
