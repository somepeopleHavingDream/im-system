package org.yangxin.im.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.yangxin.im.codec.config.BootstrapConfig;

public class ImServer {
    private final BootstrapConfig.TcpConfig config;
    private final ServerBootstrap bootstrap;

    public ImServer(BootstrapConfig.TcpConfig config) {
        this.config = config;

        EventLoopGroup mainGroup = new NioEventLoopGroup(config.getBossThreadSize());
        EventLoopGroup workerGroup = new NioEventLoopGroup(config.getWorkThreadSize());

        bootstrap = new ServerBootstrap();
        bootstrap.group(mainGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                    }
                });
    }

    public void start() {
        bootstrap.bind(config.getTcpPort());
    }
}
