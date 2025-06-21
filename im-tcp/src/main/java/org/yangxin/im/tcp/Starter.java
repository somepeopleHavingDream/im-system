package org.yangxin.im.tcp;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;
import org.yangxin.im.codec.config.BootstrapConfig;
import org.yangxin.im.tcp.receiver.MessageReceiver;
import org.yangxin.im.tcp.redis.RedisManager;
import org.yangxin.im.tcp.register.RegistryZk;
import org.yangxin.im.tcp.register.ZKit;
import org.yangxin.im.tcp.server.ImServer;
import org.yangxin.im.tcp.server.ImWebSocketServer;
import org.yangxin.im.tcp.util.MqFactory;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class Starter {
    public static void main(String[] args) {
        if (args.length > 0) {
            Starter.start(args[0]);
        }
    }

    private static void start(String path) {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = Files.newInputStream(Paths.get(path));
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new ImServer(bootstrapConfig.getIm()).start();
            new ImWebSocketServer(bootstrapConfig.getIm()).start();

            RedisManager.init(bootstrapConfig);
            MqFactory.init(bootstrapConfig.getIm().getRabbitmq());
            MessageReceiver.init(bootstrapConfig.getIm().getBrokerId() + "");
            registerZk(bootstrapConfig);
        } catch (Exception e) {
            Starter.log.error(e.getMessage(), e);
            System.exit(500);
        }
    }

    public static void registerZk(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getIm().getZkConfig().getZkAddr(), config.getIm().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        RegistryZk registryZk = new RegistryZk(zKit, hostAddress, config.getIm());

        Thread thread = new Thread(registryZk);
        thread.start();
    }
}
