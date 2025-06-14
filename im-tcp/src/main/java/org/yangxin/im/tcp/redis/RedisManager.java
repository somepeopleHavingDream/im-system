package org.yangxin.im.tcp.redis;

import org.redisson.api.RedissonClient;
import org.yangxin.im.codec.config.BootstrapConfig;
import org.yangxin.im.tcp.receive.UserLoginMessageListener;

public class RedisManager {
    private static RedissonClient redissonClient;

    public static void init(BootstrapConfig config) {
        Integer loginModel = config.getIm().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        RedisManager.redissonClient = singleClientStrategy.getRedissonClient(config.getIm().getRedis());
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();
    }

    public static RedissonClient getRedissonClient() {
        return RedisManager.redissonClient;
    }
}
