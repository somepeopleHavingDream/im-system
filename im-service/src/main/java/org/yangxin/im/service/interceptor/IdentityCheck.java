package org.yangxin.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yangxin.im.common.BaseErrorCode;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.GateWayErrorCode;
import org.yangxin.im.common.exception.ApplicationExceptionEnum;
import org.yangxin.im.common.util.SigAPI;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class IdentityCheck {
    private final StringRedisTemplate stringRedisTemplate;

    public ApplicationExceptionEnum checkUserSign(String identifier, String appId, String userSig) {
        String key = appId + ":" + Constants.RedisConstants.userSign + ":" + identifier + userSig;
        String cacheUserSig = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isBlank(cacheUserSig) && Long.parseLong(cacheUserSig) > System.currentTimeMillis() / 1000) {
            return BaseErrorCode.SUCCESS;
        }

        // 获取秘钥
//        String privateKey = appConfig.getPrivateKey();

        // 根据 appid + 秘钥创建 sigApi
//        SigAPI sigAPI = new SigAPI(Long.parseLong(appId), privateKey);

        // 调用 sigApi 对 userSig 解密
        JSONObject jsonObject = SigAPI.decodeUserSig(userSig);

        // 取出解密后的 appid 和操作人和过期时间做匹配，不通过则提示错误
        long expireTime = 0L;
        long expireSec = 0L;
        String decodedAppId = "";
        String decodedIdentifier = "";

        try {
            decodedAppId = jsonObject.getString("TLS.appId");
            decodedIdentifier = jsonObject.getString("TLS.identifier");
            String expireStr = jsonObject.get("TLS.expire").toString();
            String expireTimeStr = jsonObject.get("TLS.expireTime").toString();
            expireSec = Long.parseLong(expireStr);
            expireTime = Long.parseLong(expireTimeStr) + expireSec;
        } catch (Exception e) {
            log.error("checkUserSign error", e);
        }

        if (!decodedIdentifier.equals(identifier)) {
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }
        if (!decodedAppId.equals(appId)) {
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }
        if (expireSec == 0L) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }
        if (expireTime < System.currentTimeMillis() / 1000) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        long etime = expireTime - System.currentTimeMillis() / 1000;
        stringRedisTemplate.opsForValue().set(key, Long.toString(expireTime), etime, TimeUnit.SECONDS);

        return BaseErrorCode.SUCCESS;
    }
}
