package org.yangxin.im.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.yangxin.im.codec.MessageDecoder;
import org.yangxin.im.codec.config.BootstrapConfig;
import org.yangxin.im.tcp.handler.HeartBeatHandler;
import org.yangxin.im.tcp.handler.NettyServerHandler;

public class ImServer {
    private final BootstrapConfig.TcpConfig config;
    private final ServerBootstrap server;

    public ImServer(BootstrapConfig.TcpConfig config) {
        this.config = config;

        EventLoopGroup mainGroup = new NioEventLoopGroup(config.getBossThreadSize());
        EventLoopGroup workerGroup = new NioEventLoopGroup(config.getWorkThreadSize());

        server = new ServerBootstrap();
        server.group(mainGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 1));
                        ch.pipeline().addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        ch.pipeline().addLast(new NettyServerHandler());
                    }
                });
    }

    public void start() {
        server.bind(config.getTcpPort());
    }
}
