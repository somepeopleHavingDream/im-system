package org.yangxin.im.service.friendship.service;


import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import org.yangxin.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * @author: Chackylee
 * @description:
 **/
public interface ImFriendShipGroupMemberService {

    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    public int doAddGroupMember(Long groupId, String toId);

    public int clearGroupMember(Long groupId);
}
