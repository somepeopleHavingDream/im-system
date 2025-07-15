package org.yangxin.im.service.user.service;

import org.yangxin.im.service.user.model.UserStatusChangeNotifyContent;

public interface ImUserStatusService {
    void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);
}
