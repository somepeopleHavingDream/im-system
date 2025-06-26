package org.yangxin.im.service.seq;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSeq {
    private final StringRedisTemplate stringRedisTemplate;

    public Long doGetSeq(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }
}
