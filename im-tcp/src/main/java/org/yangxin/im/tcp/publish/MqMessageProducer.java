package org.yangxin.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.yangxin.im.codec.proto.Message;
import org.yangxin.im.codec.proto.MessageHeader;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.tcp.util.MqFactory;

@Slf4j
public class MqMessageProducer {
    public static void sendMessage(Message message, Integer command) {
        Channel channel;
        String channelName = Constants.RabbitConstants.Im2MessageService;
        if (command.toString().startsWith("2")) {
            channelName = Constants.RabbitConstants.Im2GroupService;
        }
        try {
            channel = MqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command", command);
            o.put("clientType", message.getMessageHeader().getClientType());
            o.put("imei", message.getMessageHeader().getImei());
            o.put("appId", message.getMessageHeader().getAppId());

            channel.basicPublish(channelName, "", null, JSON.toJSONString(o).getBytes());
        } catch (Exception e) {
            log.error("发送消息出现异常", e);
        }
    }

    public static void sendMessage(Object message, MessageHeader messageHeader, Integer command) {
        Channel channel;
        String channelName = Constants.RabbitConstants.Im2MessageService;
        if (command.toString().startsWith("2")) {
            channelName = Constants.RabbitConstants.Im2GroupService;
        }
        try {
            channel = MqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message);
            o.put("command", command);
            o.put("clientType", messageHeader.getClientType());
            o.put("imei", messageHeader.getImei());
            o.put("appId", messageHeader.getAppId());

            channel.basicPublish(channelName, "", null, JSON.toJSONString(o).getBytes());
        } catch (Exception e) {
            log.error("发送消息出现异常", e);
        }
    }
}
