package org.yangxin.im.tcp.receive;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.tcp.util.MqFactory;

@Slf4j
public class MessageReceiver {
    private static String brokerId;

    private static void startReceiveMessage() {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MessageService2Im + brokerId);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im + brokerId, true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im + brokerId, Constants.RabbitConstants.MessageService2Im, brokerId);
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im + brokerId, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    // 处理消息服务发来的消息
                    String msgStr = new String(body);
                    log.info(msgStr);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReceiver.brokerId)) {
            MessageReceiver.brokerId = brokerId;
        }
        startReceiveMessage();
    }
}
