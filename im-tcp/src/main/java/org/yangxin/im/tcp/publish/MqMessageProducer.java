package org.yangxin.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.yangxin.im.tcp.util.MqFactory;

@Slf4j
public class MqMessageProducer {
    public static void sendMessage(String message) {
        Channel channel = null;
        String channelName = "";
        try {
            channel = MqFactory.getChannel(channelName);
            channel.basicPublish(channelName, "", null, JSONObject.toJSONString(message).getBytes());
        } catch (Exception e) {
            log.error("发送消息出现异常", e);
        }
    }
}
