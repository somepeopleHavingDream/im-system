package org.yangxin.im.service.friendship.model.req;

import lombok.Data;
import org.yangxin.im.common.model.RequestBase;


@Data
public class ApproverFriendRequestReq extends RequestBase {

    private Long id;

    //1同意 2拒绝
    private Integer status;
}
