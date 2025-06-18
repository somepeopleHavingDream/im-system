package org.yangxin.im.service.config;

import lombok.RequiredArgsConstructor;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yangxin.im.common.config.AppConfig;
import org.yangxin.im.common.route.RouteHandle;
import org.yangxin.im.common.route.algorithm.RandomHandle;

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
        return new RandomHandle();
    }
}
