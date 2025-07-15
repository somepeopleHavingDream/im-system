package org.yangxin.im.service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.yangxin.im.common.constant.Constants;

@Service
@RequiredArgsConstructor
public class WriteUserSeq {
    private final RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId, String userId, String type, Long seq) {
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key, type, seq);
    }
}
