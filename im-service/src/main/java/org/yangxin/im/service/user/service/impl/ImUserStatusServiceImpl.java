package org.yangxin.im.service.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.pack.user.UserStatusChangeNotifyPack;
import org.yangxin.im.common.enums.command.UserEventCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.UserSession;
import org.yangxin.im.service.friendship.service.ImFriendService;
import org.yangxin.im.service.user.model.UserStatusChangeNotifyContent;
import org.yangxin.im.service.user.service.ImUserStatusService;
import org.yangxin.im.service.util.MessageProducer;
import org.yangxin.im.service.util.UserSessionUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImUserStatusServiceImpl implements ImUserStatusService {
    private final UserSessionUtil userSessionUtil;
    private final MessageProducer messageProducer;
    private final ImFriendService imFriendService;

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

//        String userKey = appId + ":" + Constants.RedisConstants.subscribe + userId;
//        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
//        for (Object key : keys) {
//            String filed = (String) key;
//            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
//            if(expire > 0 && expire > System.currentTimeMillis()){
//                messageProducer.sendToUser(filed,UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
//                        pack,appId);
//            }else{
//                stringRedisTemplate.opsForHash().delete(userKey,filed);
//            }
//        }
    }
}
