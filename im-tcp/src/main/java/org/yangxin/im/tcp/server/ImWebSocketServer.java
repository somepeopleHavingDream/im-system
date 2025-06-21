package org.yangxin.im.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.yangxin.im.codec.WebSocketMessageDecoder;
import org.yangxin.im.codec.WebSocketMessageEncoder;
import org.yangxin.im.codec.config.BootstrapConfig;
import org.yangxin.im.tcp.handler.NettyServerHandler;

@Slf4j
public class ImWebSocketServer {
    private final BootstrapConfig.TcpConfig config;
    private final ServerBootstrap bootstrap;

    public ImWebSocketServer(BootstrapConfig.TcpConfig config) {
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
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // websocket 基于http协议，所以要有http编解码器
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        // 对写大数据流的支持
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        // 几乎在netty中的编程，都会使用到此handler
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                        /*
                          websocket 服务器处理的协议，用于指定给客户端连接访问的路由 : /ws
                          本handler会帮你处理一些繁重的复杂的事
                          会帮你处理握手动作： handshaking（close, ping, pong） ping + pong = 心跳
                          对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
                         */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                        pipeline.addLast(new WebSocketMessageDecoder());
                        pipeline.addLast(new WebSocketMessageEncoder());
                        pipeline.addLast(new NettyServerHandler(config.getBrokerId()));
                    }
                });
    }

    public void start() {
        bootstrap.bind(config.getWebSocketPort());
    }
}
