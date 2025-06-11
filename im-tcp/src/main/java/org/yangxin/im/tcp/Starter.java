package org.yangxin.im.tcp;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yangxin.im.codec.config.BootstrapConfig;
import org.yangxin.im.tcp.redis.RedisManager;
import org.yangxin.im.tcp.server.ImServer;
import org.yangxin.im.tcp.server.ImWebSocketServer;

import java.io.InputStream;
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
        } catch (Exception e) {
            Starter.log.error(e.getMessage(), e);
            System.exit(500);
        }
    }
}
