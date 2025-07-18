package org.yangxin.im.service.user.model.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.yangxin.im.common.model.RequestBase;

import javax.validation.constraints.NotEmpty;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteUserReq extends RequestBase {

    @NotEmpty(message = "用户id不能为空")
    private List<String> userId;
}
