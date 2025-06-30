package org.yangxin.im.codec.pack.message;

import lombok.Data;

@Data
public class MessageReadedPack {
    private long messageSequence;
    private String fromId;
    private String toId;
    private Integer conversationType;
}
