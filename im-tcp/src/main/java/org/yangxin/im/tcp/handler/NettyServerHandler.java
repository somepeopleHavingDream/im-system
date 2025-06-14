package org.yangxin.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.yangxin.im.codec.pack.LoginPack;
import org.yangxin.im.codec.proto.Message;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.ImConnectStatusEnum;
import org.yangxin.im.common.enums.command.SystemCommand;
import org.yangxin.im.common.model.UserSession;
import org.yangxin.im.tcp.redis.RedisManager;
import org.yangxin.im.tcp.util.SessionSocketHolder;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    private final Integer brokerId;

    public NettyServerHandler(Integer brokerId) {
        this.brokerId = brokerId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        Integer command = msg.getMessageHeader().getCommand();
        // 登录 command
        if (command == SystemCommand.LOGIN.getCommand()) {
            LoginPack loginPack = JSON.parseObject(JSON.toJSONString(msg.getMessagePack()), LoginPack.class);
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(msg.getMessageHeader().getClientType());
            // 将 channel 存起来

            // Redis map
            UserSession userSession = new UserSession();
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            } catch (UnknownHostException e) {
                log.error(e.getMessage(), e);
            }

            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(msg.getMessageHeader().getClientType() + "", JSONObject.toJSONString(userSession));

            SessionSocketHolder.put(msg.getMessageHeader().getAppId(), loginPack.getUserId(), msg.getMessageHeader().getClientType(), (NioSocketChannel) ctx.channel());
        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }
    }
}
