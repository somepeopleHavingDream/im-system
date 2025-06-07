package org.yangxin.im.service.friendship.model.req;

import lombok.Data;
import org.yangxin.im.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class DeleteFriendShipGroupMemberReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotBlank(message = "分组名称不能为空")
    private String groupName;

    @NotEmpty(message = "请选择用户")
    private List<String> toIds;


}
