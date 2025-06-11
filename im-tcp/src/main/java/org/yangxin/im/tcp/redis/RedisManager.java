package org.yangxin.im.tcp.redis;

import org.redisson.api.RedissonClient;
import org.yangxin.im.codec.config.BootstrapConfig;

public class RedisManager {
    private static RedissonClient redissonClient;

    public static void init(BootstrapConfig config) {
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        RedisManager.redissonClient = singleClientStrategy.getRedissonClient(config.getIm().getRedis());
    }

    public static RedissonClient getRedissonClient() {
        return RedisManager.redissonClient;
    }
}
