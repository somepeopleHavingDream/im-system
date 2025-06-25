package org.yangxin.im.message.model;

import lombok.Data;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.message.dao.ImMessageBodyEntity;

@Data
public class DoStoreP2PMessageDto {
    private MessageContent messageContent;
    private ImMessageBodyEntity imMessageBodyEntity;
}
