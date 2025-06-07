package org.yangxin.im.service.friendship.model.req;

import lombok.Data;
import org.yangxin.im.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class AddFriendReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotNull(message = "toItem不能为空")
    private FriendDto toItem;

}
