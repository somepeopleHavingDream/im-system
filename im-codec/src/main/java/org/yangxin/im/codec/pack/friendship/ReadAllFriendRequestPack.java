package org.yangxin.im.codec.pack.friendship;

import lombok.Data;

@Data
public class ReadAllFriendRequestPack {

    private String fromId;

    private Long sequence;
}
