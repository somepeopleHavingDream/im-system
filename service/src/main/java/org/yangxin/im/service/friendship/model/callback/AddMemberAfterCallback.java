package org.yangxin.im.service.friendship.model.callback;

import lombok.Data;
import org.yangxin.im.service.group.model.resp.AddMemberResp;

import java.util.List;

@Data
public class AddMemberAfterCallback {
    private String groupId;
    private Integer groupType;
    private String operater;
    private List<AddMemberResp> memberId;
}
