package org.yangxin.im.common.util;

import org.yangxin.im.common.BaseErrorCode;
import org.yangxin.im.common.exception.ApplicationException;
import org.yangxin.im.common.route.RouteInfo;

public class RouteInfoParseUtil {
    public static RouteInfo parse(String info) {
        try {
            String[] serverInfo = info.split(":");
            return new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1]));
        } catch (Exception e) {
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR);
        }
    }
}
