package org.yangxin.im.service.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yangxin.im.common.ClientType;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.route.RouteHandle;
import org.yangxin.im.common.route.RouteInfo;
import org.yangxin.im.common.util.RouteInfoParseUtil;
import org.yangxin.im.service.user.model.req.DeleteUserReq;
import org.yangxin.im.service.user.model.req.GetUserSequenceReq;
import org.yangxin.im.service.user.model.req.ImportUserReq;
import org.yangxin.im.service.user.model.req.LoginReq;
import org.yangxin.im.service.user.service.ImUserService;
import org.yangxin.im.service.util.ZKit;

import java.util.List;


@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("v1/user")
@RequiredArgsConstructor
public class ImUserController {
    private final ImUserService imUserService;
    private final RouteHandle routeHandle;
    private final ZKit zkKit;

    @RequestMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId) {
        return imUserService.importUser(req);
    }

    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }

    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);
        ResponseVO login = imUserService.login(req);
        if (login.isOk()) {
            List<String> allNode;
            if (req.getClientType() == ClientType.WEB.getCode()) {
                allNode = zkKit.getAllWebNode();
            } else {
                allNode = zkKit.getAllTcpNode();
            }
            String s = routeHandle.routeServer(allNode, req.getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }
        return ResponseVO.errorResponse();
    }

    @RequestMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody @Validated
                                      GetUserSequenceReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.getUserSequence(req);
    }
}
