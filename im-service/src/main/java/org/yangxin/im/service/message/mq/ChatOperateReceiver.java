package org.yangxin.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.command.MessageCommand;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.common.model.message.MessageReadedContent;
import org.yangxin.im.common.model.message.MessageReceiveAckContent;
import org.yangxin.im.service.message.service.MessageSyncService;
import org.yangxin.im.service.message.service.P2PMessageService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatOperateReceiver {
    private final P2PMessageService p2pMessageService;
    private final MessageSyncService messageSyncService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.Im2MessageService),
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService)
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message, @Headers Map<String, Object> headers, Channel channel) throws IOException {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("onChatMessage {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                // 处理消息
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                p2pMessageService.process(messageContent);
            } else if (command.equals(MessageCommand.MSG_RECEIVE_ACK.getCommand())) {
                // 消息接收确认
                MessageReceiveAckContent messageContent = jsonObject.toJavaObject(MessageReceiveAckContent.class);
                messageSyncService.receiveMark(messageContent);
            } else if (command.equals(MessageCommand.MSG_READED.getCommand())) {
                MessageReadedContent messageContent = jsonObject.toJavaObject(MessageReadedContent.class);
                messageSyncService.readMark(messageContent);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("onChatMessage error {}", msg, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
