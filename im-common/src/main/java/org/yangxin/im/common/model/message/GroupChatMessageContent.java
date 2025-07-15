package org.yangxin.im.common.model.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupChatMessageContent extends MessageContent {
    private String groupId;
    private List<String> memberId;
}
