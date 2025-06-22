package org.yangxin.im.service.message.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.yangxin.im.common.constant.Constants;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
public class ChatOperateReceiver {
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.Im2MessageService),
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService)
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message, @Headers Map<String, Object> headers, Channel channel) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("onChatMessage:{}", msg);
    }
}
