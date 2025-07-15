package org.yangxin.im.service.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.pack.user.UserStatusChangeNotifyPack;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.command.UserEventCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.UserSession;
import org.yangxin.im.service.friendship.service.ImFriendService;
import org.yangxin.im.service.user.model.UserStatusChangeNotifyContent;
import org.yangxin.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import org.yangxin.im.service.user.service.ImUserStatusService;
import org.yangxin.im.service.util.MessageProducer;
import org.yangxin.im.service.util.UserSessionUtil;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImUserStatusServiceImpl implements ImUserStatusService {
    private final UserSessionUtil userSessionUtil;
    private final MessageProducer messageProducer;
    private final ImFriendService imFriendService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content) {
        List<UserSession> sessionList = userSessionUtil.getUserSessions(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content, userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(sessionList);

        // 发送给自己的同步端
        syncSender(userStatusChangeNotifyPack, content.getUserId(), content);
        // 同步给好友和订阅了自己的人
        dispatcher(userStatusChangeNotifyPack, content.getUserId(), content.getAppId());
    }

    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {
        // A
        // Z
        // A - B C D
        // C：A Z F
        //hash
        // B - [A:xxxx,C:xxxx]
        // C - []
        // D - []
        long subExpireTime = 0L;
        if (req != null && req.getSubTime() > 0) {
            subExpireTime = System.currentTimeMillis() + req.getSubTime();
        }

        for (String beSubUserId : req.getSubUserId()) {
            String userKey = req.getAppId() + ":" + Constants.RedisConstants.subscribe + ":" + beSubUserId;
            stringRedisTemplate.opsForHash().put(userKey, req.getOperater(), Long.toString(subExpireTime));
        }
    }

    private void syncSender(Object pack, String userId, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(userId, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY, pack,
                clientInfo);
    }

    private void dispatcher(Object pack, String userId, Integer appId) {
        List<String> allFriendId = imFriendService.getAllFriendId(userId, appId);
        for (String fid : allFriendId) {
            messageProducer.sendToUser(fid, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    pack, appId);
        }

        String userKey = appId + ":" + Constants.RedisConstants.subscribe + ":" + userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key : keys) {
            String filed = (String) key;
            long expire = Long.parseLong((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack, appId);
            } else {
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }
    }
}
