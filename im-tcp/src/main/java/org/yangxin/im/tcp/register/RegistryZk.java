package org.yangxin.im.tcp.register;

import lombok.extern.slf4j.Slf4j;
import org.yangxin.im.codec.config.BootstrapConfig;
import org.yangxin.im.common.constant.Constants;

@Slf4j
public class RegistryZk implements Runnable {
    private final ZKit zKit;
    private final String ip;
    private final BootstrapConfig.TcpConfig tcpConfig;

    public RegistryZk(ZKit zKit, String ip, BootstrapConfig.TcpConfig tcpConfig) {
        this.zKit = zKit;
        this.ip = ip;
        this.tcpConfig = tcpConfig;
    }

    @Override
    public void run() {
        zKit.createRootNode();

        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        log.info("RegistryZk run tcp ok.");

        String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebSocketPort();
        zKit.createNode(webPath);
        log.info("RegistryZk run web ok.");
    }
}
