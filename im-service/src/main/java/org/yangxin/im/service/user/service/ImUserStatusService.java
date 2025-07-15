package org.yangxin.im.service.user.service;

import org.yangxin.im.service.user.model.UserStatusChangeNotifyContent;
import org.yangxin.im.service.user.model.req.SubscribeUserOnlineStatusReq;

public interface ImUserStatusService {
    void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);
}
