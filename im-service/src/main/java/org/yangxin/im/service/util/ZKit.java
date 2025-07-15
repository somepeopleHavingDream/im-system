package org.yangxin.im.service.util;

import lombok.RequiredArgsConstructor;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.stereotype.Component;
import org.yangxin.im.common.constant.Constants;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ZKit {
    private final ZkClient zkClient;

    /**
     * get all TCP server node from zookeeper
     */
    public List<String> getAllTcpNode() {
        //        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
    }

    /**
     * get all WEB server node from zookeeper
     */
    public List<String> getAllWebNode() {
        //        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
    }
}
