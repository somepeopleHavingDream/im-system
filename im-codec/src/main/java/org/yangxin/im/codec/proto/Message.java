package org.yangxin.im.codec.proto;

import lombok.Data;

@Data
public class Message {
    private MessageHeader messageHeader;
    private Object messagePack;
}
