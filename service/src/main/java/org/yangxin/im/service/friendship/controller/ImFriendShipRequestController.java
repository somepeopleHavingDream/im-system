package org.yangxin.im.service.friendship.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.service.friendship.model.req.ApproverFriendRequestReq;
import org.yangxin.im.service.friendship.model.req.GetFriendShipRequestReq;
import org.yangxin.im.service.friendship.model.req.ReadFriendShipRequestReq;
import org.yangxin.im.service.friendship.service.ImFriendShipRequestService;


@RestController
@RequestMapping("v1/friendshipRequest")
public class ImFriendShipRequestController {

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @RequestMapping("/approveFriendRequest")
    public ResponseVO approveFriendRequest(@RequestBody @Validated
                                           ApproverFriendRequestReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        return imFriendShipRequestService.approverFriendRequest(req);
    }

    @RequestMapping("/getFriendRequest")
    public ResponseVO getFriendRequest(@RequestBody @Validated GetFriendShipRequestReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipRequestService.getFriendRequest(req.getFromId(), req.getAppId());
    }

    @RequestMapping("/readFriendShipRequestReq")
    public ResponseVO readFriendShipRequestReq(@RequestBody @Validated ReadFriendShipRequestReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipRequestService.readFriendShipRequestReq(req);
    }


}
