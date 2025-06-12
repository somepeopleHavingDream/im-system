package org.yangxin.im.tcp.receive;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.tcp.util.MqFactory;

@Slf4j
public class MessageReceiver {
    private static void startReceiveMessage() {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MessageService2Im);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im, true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im, Constants.RabbitConstants.MessageService2Im, "");
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im, false, new DefaultConsumer(channel) {
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

    public static void init() {
        startReceiveMessage();
    }
}
