package org.yangxin.im.service.friendship.model.req;

import lombok.Data;
import org.yangxin.im.common.model.RequestBase;

import javax.validation.constraints.NotBlank;


@Data
public class DeleteFriendReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotBlank(message = "toId不能为空")
    private String toId;

}
