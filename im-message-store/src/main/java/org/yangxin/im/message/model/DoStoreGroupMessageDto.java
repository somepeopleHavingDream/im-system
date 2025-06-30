package org.yangxin.im.message.model;

import lombok.Data;
import org.yangxin.im.common.model.message.GroupChatMessageContent;
import org.yangxin.im.message.dao.ImMessageBodyEntity;

@Data
public class DoStoreGroupMessageDto {
    private GroupChatMessageContent groupChatMessageContent;
    private ImMessageBodyEntity imMessageBodyEntity;
}
