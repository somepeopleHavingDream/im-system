package org.yangxin.im.service.friendship.service;

import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.service.friendship.dao.ImFriendShipGroupEntity;
import org.yangxin.im.service.friendship.model.req.AddFriendShipGroupReq;
import org.yangxin.im.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * @author: Chackylee
 * @description:
 **/
public interface ImFriendShipGroupService {

    public ResponseVO addGroup(AddFriendShipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);


}
