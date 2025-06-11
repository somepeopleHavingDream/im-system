package org.yangxin.im.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.tcp.util.SessionSocketHolder;

@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private final Long heartBeatTime;

    public HeartBeatHandler(Long heartBeatTime) {
        this.heartBeatTime = heartBeatTime;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 判断 evt 是否是 IdleStateEvent （用于触发用户事件，包含：读空闲/写空闲/读写空闲）
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                HeartBeatHandler.log.info("读空闲");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                HeartBeatHandler.log.info("写空闲");
            } else if (event.state() == IdleState.ALL_IDLE) {
                Long lastReadTime = (Long) ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).get();
                long now = System.currentTimeMillis();

                if (lastReadTime != null && now - lastReadTime > heartBeatTime) {
                    // 退后台逻辑
                    SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
                }
            }
        }
    }
}
