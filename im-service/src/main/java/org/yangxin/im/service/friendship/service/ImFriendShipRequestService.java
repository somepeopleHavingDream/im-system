package org.yangxin.im.service.friendship.service;

import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.service.friendship.model.req.ApproverFriendRequestReq;
import org.yangxin.im.service.friendship.model.req.FriendDto;
import org.yangxin.im.service.friendship.model.req.ReadFriendShipRequestReq;

public interface ImFriendShipRequestService {

    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId);

    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req);

    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    public ResponseVO getFriendRequest(String fromId, Integer appId);
}
