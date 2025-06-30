package org.yangxin.im.common.model.message;

import lombok.Data;

@Data
public class DoStoreGroupMessageDto {
    private GroupChatMessageContent groupChatMessageContent;
    private ImMessageBody messageBody;
}
