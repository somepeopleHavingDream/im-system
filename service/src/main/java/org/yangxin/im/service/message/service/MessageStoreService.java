package org.yangxin.im.service.message.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yangxin.im.common.config.AppConfig;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.ConversationTypeEnum;
import org.yangxin.im.common.enums.DelFlagEnum;
import org.yangxin.im.common.model.message.*;
import org.yangxin.im.service.conversation.service.ConversationService;
import org.yangxin.im.service.util.SnowflakeIdWorker;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MessageStoreService {
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private final ConversationService conversationService;
    private final AppConfig appConfig;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {
        ImMessageBody imMessageBodyEntity = extractMessageBody(messageContent);
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(imMessageBodyEntity);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
        // 发送 mq 消息
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage, "", JSON.toJSONString(dto));
    }

    public ImMessageBody extractMessageBody(MessageContent messageContent) {
        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(SnowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent) {
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setMessageBody(imMessageBody);
        dto.setGroupChatMessageContent(messageContent);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreGroupMessage, "", JSON.toJSONString(dto));
        messageContent.setMessageKey(imMessageBody.getMessageKey());
    }

    public void setMessageFromMessageIdCache(Integer appId, String messageId, Object messageContent) {
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(messageContent), 300, TimeUnit.SECONDS);
    }

    public <T> T getMessageFromMessageIdCache(Integer appId, String messageId, Class<T> clazz) {
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }

    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {
        String fromKey =
                offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        String toKey =
                offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getToId();

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        if (Optional.ofNullable(operations.zCard(fromKey)).orElse(0L) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(fromKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(ConversationTypeEnum.P2P.getCode()
                , offlineMessage.getFromId(), offlineMessage.getToId()));
        operations.add(fromKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());
        if (Optional.ofNullable(operations.zCard(toKey)).orElse(0L) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(toKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(ConversationTypeEnum.P2P.getCode()
                , offlineMessage.getToId(), offlineMessage.getFromId()));
        operations.add(toKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());
    }

    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage, List<String> memberIds) {
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());
        for (String memberId : memberIds) {
            String toKey =
                    offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(ConversationTypeEnum.GROUP.getCode()
                    , memberId, offlineMessage.getToId()));
            if (Optional.ofNullable(operations.zCard(toKey)).orElse(0L) > appConfig.getOfflineMessageCount()) {
                operations.removeRange(toKey, 0, 0);
            }
            operations.add(toKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());
        }
    }
}
