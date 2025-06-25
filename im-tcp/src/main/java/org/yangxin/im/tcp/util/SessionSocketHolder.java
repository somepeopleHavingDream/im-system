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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("DuplicatedCode")
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId, Integer clientType, String imei, NioSocketChannel channel) {
        UserClientDto dto = new UserClientDto();
        dto.setImei(imei);
        dto.setAppId(appId);
        dto.setClientType(clientType);
        dto.setUserId(userId);

        SessionSocketHolder.CHANNEL_MAP.put(dto, channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto dto = new UserClientDto();
        dto.setImei(imei);
        dto.setAppId(appId);
        dto.setClientType(clientType);
        dto.setUserId(userId);

        return SessionSocketHolder.CHANNEL_MAP.get(dto);
    }

    public static List<NioSocketChannel> get(Integer appId, String id) {
        Set<UserClientDto> channelInfos = CHANNEL_MAP.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();
        channelInfos.forEach(channel -> {
            if (channel.getAppId().equals(appId) && id.equals(channel.getUserId())) {
                channels.add(CHANNEL_MAP.get(channel));
            }
        });
        return channels;
    }

    public static void remove(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto dto = new UserClientDto();
        dto.setImei(imei);
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
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        SessionSocketHolder.remove(appId, userId, clientType, imei);

        // redis 删除
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map =
                redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType + ":" + imei);

        nioSocketChannel.close();
    }

    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        SessionSocketHolder.remove(appId, userId, clientType, imei);

        // redis 删除
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map =
                redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        String sessionStr = map.get(clientType.toString() + ":" + imei);

        if (!StringUtils.isBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType + ":" + imei, JSONObject.toJSONString(userSession));
        }

        nioSocketChannel.close();
    }
}
