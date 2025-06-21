package org.yangxin.app.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yangxin.im.common.ResponseVO;

@RestController
@Slf4j
public class CallbackController {
    @RequestMapping("/callback")
    public ResponseVO<?> callback(@RequestBody Object req, String command, Integer appId) {
        log.info("callback {} {} {}", appId, command, JSONObject.toJSONString(req));
        return ResponseVO.successResponse();
    }
}
