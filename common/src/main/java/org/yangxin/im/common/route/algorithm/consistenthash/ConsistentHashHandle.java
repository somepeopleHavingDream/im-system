package org.yangxin.im.common.route.algorithm.consistenthash;

import lombok.Setter;
import org.yangxin.im.common.route.RouteHandle;

import java.util.List;

@Setter
public class ConsistentHashHandle implements RouteHandle {
    private AbstractConsistentHash hash;

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values, key);
    }
}
