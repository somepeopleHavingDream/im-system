package org.yangxin.im.service.util;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.ImConnectStatusEnum;
import org.yangxin.im.common.model.UserSession;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserSessionUtil {
    private final StringRedisTemplate stringRedisTemplate;

    // 获取用户所有的 session
    public List<UserSession> getUserSessions(Integer appId, String userId) {
        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);
        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o : values) {
            String str = (String) o;
            UserSession session = JSONObject.parseObject(str, UserSession.class);
            if (Objects.equals(session.getConnectState(), ImConnectStatusEnum.ONLINE_STATUS.getCode())) {
                list.add(session);
            }
        }
        return list;
    }

    // 获取用户除了本端的 session
    public UserSession getUserSessions(Integer appId, String userId, Integer clientType, String imei) {
        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        return JSONObject.parseObject((String) o, UserSession.class);
    }
}
