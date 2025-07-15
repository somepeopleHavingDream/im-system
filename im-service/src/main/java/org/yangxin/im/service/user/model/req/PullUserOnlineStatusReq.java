package org.yangxin.im.service.user.model.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.yangxin.im.common.model.RequestBase;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PullUserOnlineStatusReq extends RequestBase {

    private List<String> userList;

}
