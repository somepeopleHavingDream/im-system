package org.yangxin.im.service.user.service;

import org.yangxin.im.service.user.model.UserStatusChangeNotifyContent;
import org.yangxin.im.service.user.model.req.PullFriendOnlineStatusReq;
import org.yangxin.im.service.user.model.req.PullUserOnlineStatusReq;
import org.yangxin.im.service.user.model.req.SetUserCustomerStatusReq;
import org.yangxin.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import org.yangxin.im.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;

public interface ImUserStatusService {
    void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomerStatus(SetUserCustomerStatusReq req);

    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);
}
