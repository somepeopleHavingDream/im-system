package org.yangxin.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.yangxin.im.codec.pack.LoginPack;
import org.yangxin.im.codec.proto.Message;
import org.yangxin.im.common.enums.command.SystemCommand;
import org.yangxin.im.tcp.util.SessionSocketHolder;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        Integer command = msg.getMessageHeader().getCommand();
        // 登录 command
        if (command == SystemCommand.LOGIN.getCommand()) {
            LoginPack loginPack = JSON.parseObject(JSON.toJSONString(msg.getMessagePack()), LoginPack.class);
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(loginPack.getUserId());
            // 将 channel 存起来
            SessionSocketHolder.put(loginPack.getUserId(), (NioSocketChannel) ctx.channel());
        }
    }
}
