package org.yangxin.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.enums.GateWayErrorCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
@Slf4j
@RequiredArgsConstructor
public class GateWayInterceptor implements HandlerInterceptor {
    private final IdentityCheck identityCheck;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // 获取 appId 、操作人、 userSign
        String appIdStr = request.getParameter("appId");
        if (StringUtils.isBlank(appIdStr)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.APPID_NOT_EXIST), response);
            return false;
        }

        String identifier = request.getParameter("identifier");
        if (StringUtils.isBlank(identifier)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.OPERATER_NOT_EXIST), response);
            return false;
        }

        String userSign = request.getParameter("userSign");
        if (StringUtils.isBlank(userSign)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_IS_ERROR), response);
            return false;
        }

        // 签名和操作人和 appid 是否匹配
//        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSign(identifier, appIdStr,
//        userSign);
//        if (applicationExceptionEnum != BaseErrorCode.SUCCESS) {
//            resp(ResponseVO.errorResponse(applicationExceptionEnum), response);
//            return false;
//        }

        return true;
    }

    private void resp(ResponseVO<?> respVo, HttpServletResponse response) {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try {
            String resp = JSONObject.toJSONString(respVo);
            writer = response.getWriter();
            writer.write(resp);
        } catch (Exception e) {
            log.error("resp error:", e);
        } finally {
            if (writer != null) {
                writer.checkError();
            }
        }
    }
}
