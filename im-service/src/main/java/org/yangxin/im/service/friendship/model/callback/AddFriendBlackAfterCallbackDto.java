package org.yangxin.im.service.friendship.model.callback;

import lombok.Data;

@Data
public class AddFriendBlackAfterCallbackDto {
    private String fromId;
    private String toId;
}
