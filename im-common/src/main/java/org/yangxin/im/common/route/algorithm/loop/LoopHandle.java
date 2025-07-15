package org.yangxin.im.common.route.algorithm.loop;

import org.yangxin.im.common.enums.UserErrorCode;
import org.yangxin.im.common.exception.ApplicationException;
import org.yangxin.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class LoopHandle implements RouteHandle {
    private final AtomicLong index = new AtomicLong(0);

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size < 1) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        long l = index.incrementAndGet() % size;
        if (l < 0) {
            l = 0L;
        }
        return values.get((int) l);
    }
}
