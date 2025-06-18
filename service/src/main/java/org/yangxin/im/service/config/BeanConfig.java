package org.yangxin.im.service.config;

import lombok.RequiredArgsConstructor;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yangxin.im.common.config.AppConfig;
import org.yangxin.im.common.route.RouteHandle;
import org.yangxin.im.common.route.algorithm.consistenthash.ConsistentHashHandle;
import org.yangxin.im.common.route.algorithm.consistenthash.TreeMapConsistentHash;

@Configuration
@RequiredArgsConstructor
public class BeanConfig {
    private final AppConfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(),
                appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandle routeHandle() {
        TreeMapConsistentHash treeMapConsistentHash = new TreeMapConsistentHash();
        ConsistentHashHandle consistentHashHandle = new ConsistentHashHandle();
        consistentHashHandle.setHash(treeMapConsistentHash);
        return consistentHashHandle;
    }
}
