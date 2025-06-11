package org.yangxin.im.tcp.util;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.yangxin.im.common.model.UserClientDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
}
