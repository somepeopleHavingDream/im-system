package org.yangxin.im.service.group.model.req;

import org.yangxin.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class GetRoleInGroupReq extends RequestBase {

    private String groupId;

    private List<String> memberId;
}
