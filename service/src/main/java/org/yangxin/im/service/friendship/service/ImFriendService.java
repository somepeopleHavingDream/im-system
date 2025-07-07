package org.yangxin.im.service.friendship.service;

import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.model.RequestBase;
import org.yangxin.im.common.model.SyncReq;
import org.yangxin.im.service.friendship.model.req.*;

@SuppressWarnings("rawtypes")
public interface ImFriendService {

    ResponseVO importFriendShip(ImporFriendShipReq req);

    ResponseVO addFriend(AddFriendReq req);

    ResponseVO updateFriend(UpdateFriendReq req);

    ResponseVO deleteFriend(DeleteFriendReq req);

    ResponseVO deleteAllFriend(DeleteFriendReq req);

    ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    ResponseVO getRelation(GetRelationReq req);

    ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId);

    ResponseVO checkFriendship(CheckFriendShipReq req);

    ResponseVO addBlack(AddFriendShipBlackReq req);

    ResponseVO deleteBlack(DeleteBlackReq req);

    ResponseVO checkBlck(CheckFriendShipReq req);

    ResponseVO syncFriendshipList(SyncReq req);
}
