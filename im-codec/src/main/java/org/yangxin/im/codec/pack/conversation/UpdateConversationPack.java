package org.yangxin.im.codec.pack.conversation;

import lombok.Data;

@Data
public class UpdateConversationPack {
    private String conversationId;
    private Integer isMute;
    private Integer isTop;
    private Integer conversationType;
    private Long sequence;
}
