package org.yangxin.im.service.friendship.model.req;

import lombok.Data;
import org.yangxin.im.common.model.RequestBase;

import javax.validation.constraints.NotBlank;


@Data
public class AddFriendShipBlackReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    private String toId;
}
