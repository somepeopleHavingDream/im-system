package org.yangxin.im.service.message.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.model.CheckSendMessageReq;
import org.yangxin.im.service.message.model.req.SendMessageReq;
import org.yangxin.im.service.message.service.P2PMessageService;

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("v1/message")
@RequiredArgsConstructor
public class MessageController {
    private final P2PMessageService p2PMessageService;

    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId) {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req) {
        return p2PMessageService.imServerPermissionCheck(req.getFromId(), req.getToId(), req.getAppId());
    }
}
