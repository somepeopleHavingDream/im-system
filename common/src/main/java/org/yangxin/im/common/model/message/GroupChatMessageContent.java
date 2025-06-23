package org.yangxin.im.common.model.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatMessageContent extends MessageContent {
    private String groupId;
}
