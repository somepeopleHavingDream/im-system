package org.yangxin.im.codec.pack.friendship;

import lombok.Data;

@Data
public class DeleteFriendGroupPack {
    public String fromId;

    private String groupName;

    /**
     * 序列号
     */
    private Long sequence;
}
