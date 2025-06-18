package org.yangxin.im.common.route.algorithm.random;

import org.yangxin.im.common.enums.UserErrorCode;
import org.yangxin.im.common.exception.ApplicationException;
import org.yangxin.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomHandle implements RouteHandle {
    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size < 1) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int i = ThreadLocalRandom.current().nextInt(size);
        return values.get(i);
    }
}
