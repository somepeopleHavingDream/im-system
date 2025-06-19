package org.yangxin.im.service.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.config.AppConfig;
import org.yangxin.im.common.util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
@Slf4j
@RequiredArgsConstructor
public class CallbackService {
    private final HttpRequestUtils httpRequestUtils;
    private final AppConfig appConfig;

    public void callback(Integer appId, String callbackCommand, String jsonBody) {
        try {
            httpRequestUtils.doPost("", Object.class, builderUrlParams(appId, callbackCommand), jsonBody, null);
        } catch (Exception e) {
            log.error("callback error {} {} {}", appId, callbackCommand, jsonBody, e);
        }
    }

    public ResponseVO beforeCallback(Integer appId, String callbackCommand, String jsonBody) {
        try {
            return httpRequestUtils.doPost("", ResponseVO.class, builderUrlParams(appId, callbackCommand), jsonBody, null);
        } catch (Exception e) {
            log.error("beforeCallback error {} {} {}", appId, callbackCommand, jsonBody, e);
            return ResponseVO.successResponse();
        }
    }

    public Map builderUrlParams(Integer appId, String command) {
        Map map = new HashMap();
        map.put("appId", appId);
        map.put("command", command);
        return map;
    }
}
