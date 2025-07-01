package org.yangxin.im.service.conversation.model;

import lombok.Getter;
import lombok.Setter;
import org.yangxin.im.common.model.RequestBase;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class DeleteConversationReq extends RequestBase {
    @NotBlank(message = "会话 id 不能为空")
    private String conversationId;

    @NotBlank(message = "fromId 不能为空")
    private String fromId;
}
