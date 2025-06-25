package org.yangxin.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.yangxin.im.codec.pack.LoginPack;
import org.yangxin.im.codec.pack.message.ChatMessageAck;
import org.yangxin.im.codec.proto.Message;
import org.yangxin.im.codec.proto.MessagePack;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.ImConnectStatusEnum;
import org.yangxin.im.common.enums.command.MessageCommand;
import org.yangxin.im.common.enums.command.SystemCommand;
import org.yangxin.im.common.model.CheckSendMessageReq;
import org.yangxin.im.common.model.UserClientDto;
import org.yangxin.im.common.model.UserSession;
import org.yangxin.im.tcp.feign.FeignMessageService;
import org.yangxin.im.tcp.publish.MqMessageProducer;
import org.yangxin.im.tcp.redis.RedisManager;
import org.yangxin.im.tcp.util.SessionSocketHolder;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    private final Integer brokerId;
    private final FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.brokerId = brokerId;
        this.feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))
                .target(FeignMessageService.class, logicUrl);
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
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(msg.getMessageHeader().getImei());

            // 将 channel 存起来
            // Redis map
            UserSession userSession = new UserSession();
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(msg.getMessageHeader().getImei());
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            } catch (UnknownHostException e) {
                log.error(e.getMessage(), e);
            }
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map =
                    redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei(),
                    JSONObject.toJSONString(userSession));
            SessionSocketHolder.put(msg.getMessageHeader().getAppId(), loginPack.getUserId(),
                    msg.getMessageHeader().getClientType(), msg.getMessageHeader().getImei(),
                    (NioSocketChannel) ctx.channel());

            UserClientDto dto = new UserClientDto();
            dto.setImei(msg.getMessageHeader().getImei());
            dto.setUserId(loginPack.getUserId());
            dto.setClientType(msg.getMessageHeader().getClientType());
            dto.setAppId(msg.getMessageHeader().getAppId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(dto));
        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand()) {
            // 调用校验消息发送方的接口，如果成功投递到 mq ，失败则直接 ack
            CheckSendMessageReq req = new CheckSendMessageReq();
            req.setAppId(msg.getMessageHeader().getAppId());
            req.setCommand(msg.getMessageHeader().getCommand());
            JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
            String fromId = jsonObject.getString("fromId");
            String toId = jsonObject.getString("toId");
            req.setFromId(fromId);
            req.setToId(toId);

            ResponseVO responseVO = feignMessageService.checkSendMessage(req);
            if (responseVO.isOk()) {
                MqMessageProducer.sendMessage(msg, command);
            } else {
                // ack
                ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                responseVO.setData(chatMessageAck);
                MessagePack<ResponseVO> ack = new MessagePack<>();
                ack.setData(responseVO);
                ack.setCommand(MessageCommand.MSG_ACK.getCommand());
                ctx.channel().writeAndFlush(ack);
            }
        } else {
            MqMessageProducer.sendMessage(msg, command);
        }
    }

    //表示 channel 处于不活动状态
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }
}
