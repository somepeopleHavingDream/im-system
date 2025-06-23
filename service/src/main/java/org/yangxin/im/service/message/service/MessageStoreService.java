package org.yangxin.im.service.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yangxin.im.common.enums.DelFlagEnum;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.service.message.dao.ImMessageBodyEntity;
import org.yangxin.im.service.message.dao.ImMessageHistoryEntity;
import org.yangxin.im.service.message.dao.mapper.ImMessageBodyMapper;
import org.yangxin.im.service.message.dao.mapper.ImMessageHistoryMapper;
import org.yangxin.im.service.util.SnowflakeIdWorker;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageStoreService {
    private final ImMessageHistoryMapper imMessageHistoryMapper;
    private final ImMessageBodyMapper imMessageBodyMapper;
    private final SnowflakeIdWorker snowflakeIdWorker;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {
        // messageContent 转化为 messageBody
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        // 插入 messageBody
        imMessageBodyMapper.insert(imMessageBodyEntity);
        // 转化为 MessageHistory
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractTopP2PMessageHistory(messageContent,
                imMessageBodyEntity);
        // 批量插入
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);

        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    public ImMessageBodyEntity extractMessageBody(MessageContent messageContent) {
        ImMessageBodyEntity messageBody = new ImMessageBodyEntity();
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

    public List<ImMessageHistoryEntity> extractTopP2PMessageHistory(MessageContent messageContent,
                                                                    ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);

        return list;
    }

}
