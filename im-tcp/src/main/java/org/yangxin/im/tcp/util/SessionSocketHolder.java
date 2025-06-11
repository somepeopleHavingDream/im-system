package org.yangxin.im.tcp.util;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.ImConnectStatusEnum;
import org.yangxin.im.common.model.UserClientDto;
import org.yangxin.im.common.model.UserSession;
import org.yangxin.im.tcp.redis.RedisManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("DuplicatedCode")
public class SessionSocketHolder {
    private static final Map<UserClientDto, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId, Integer clientType, NioSocketChannel channel) {
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setClientType(clientType);
        dto.setUserId(userId);

        SessionSocketHolder.CHANNEL_MAP.put(dto, channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType) {
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setClientType(clientType);
        dto.setUserId(userId);

        return SessionSocketHolder.CHANNEL_MAP.get(dto);
    }

    public static void remove(Integer appId, String userId, Integer clientType) {
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setClientType(clientType);
        dto.setUserId(userId);

        SessionSocketHolder.CHANNEL_MAP.remove(dto);
    }

    public static void remove(NioSocketChannel channel) {
        CHANNEL_MAP.entrySet().removeIf(entry -> entry.getValue() == channel);
    }

    public static void removeUserSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        SessionSocketHolder.remove(appId, userId, clientType);

        // redis 删除
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType);

        nioSocketChannel.close();
    }

    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        SessionSocketHolder.remove(appId, userId, clientType);

        // redis 删除
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        String sessionStr = map.get(clientType.toString());

        if (!StringUtils.isBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType.toString(), JSONObject.toJSONString(userSession));
        }

        nioSocketChannel.close();
    }
}
