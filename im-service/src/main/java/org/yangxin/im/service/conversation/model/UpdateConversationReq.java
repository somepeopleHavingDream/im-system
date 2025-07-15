package org.yangxin.im.service.conversation.model;

import lombok.Getter;
import lombok.Setter;
import org.yangxin.im.common.model.RequestBase;

@Getter
@Setter
public class UpdateConversationReq extends RequestBase {
    private String conversationId;
    private Integer isMute;
    private Integer isTop;
    private String fromId;
}
