package org.yangxin.im.message.mq;

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
import org.springframework.stereotype.Service;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.message.dao.ImMessageBodyEntity;
import org.yangxin.im.message.model.DoStoreGroupMessageDto;
import org.yangxin.im.message.service.StoreMessageService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreGroupMessageReceiver {
    private final StoreMessageService storeMessageService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.StoreGroupMessage),
                    exchange = @Exchange(value = Constants.RabbitConstants.StoreGroupMessage)
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message, @Headers Map<String, Object> headers, Channel channel) throws IOException {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("onChatMessage {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            DoStoreGroupMessageDto doStoreP2PMessageDto = jsonObject.toJavaObject(DoStoreGroupMessageDto.class);
            ImMessageBodyEntity messageBody = jsonObject.getObject("messageBody", ImMessageBodyEntity.class);
            doStoreP2PMessageDto.setImMessageBodyEntity(messageBody);
            storeMessageService.doStoreGroupMessage(doStoreP2PMessageDto);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("onChatMessage error {}", msg, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
