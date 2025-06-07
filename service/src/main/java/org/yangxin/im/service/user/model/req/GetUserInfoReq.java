package org.yangxin.im.service.user.model.req;

import org.yangxin.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;


@Data
public class GetUserInfoReq extends RequestBase {

    private List<String> userIds;


}
