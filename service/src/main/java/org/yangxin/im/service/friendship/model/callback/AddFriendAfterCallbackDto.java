package org.yangxin.im.service.friendship.model.callback;

import lombok.Data;
import org.yangxin.im.service.friendship.model.req.FriendDto;

@Data
public class AddFriendAfterCallbackDto {
    private String fromId;
    private FriendDto toItem;
}
