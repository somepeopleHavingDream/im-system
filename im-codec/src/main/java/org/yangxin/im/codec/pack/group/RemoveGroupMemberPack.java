package org.yangxin.im.codec.pack.group;

import lombok.Data;

@Data
public class RemoveGroupMemberPack {

    private String groupId;

    private String member;

}
