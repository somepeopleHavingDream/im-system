package org.yangxin.im.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yangxin.im.common.model.message.GroupChatMessageContent;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.message.dao.ImGroupMessageHistoryEntity;
import org.yangxin.im.message.dao.ImMessageBodyEntity;
import org.yangxin.im.message.dao.ImMessageHistoryEntity;
import org.yangxin.im.message.dao.mapper.ImGroupMessageHistoryMapper;
import org.yangxin.im.message.dao.mapper.ImMessageBodyMapper;
import org.yangxin.im.message.dao.mapper.ImMessageHistoryMapper;
import org.yangxin.im.message.model.DoStoreGroupMessageDto;
import org.yangxin.im.message.model.DoStoreP2PMessageDto;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreMessageService {
    private final ImMessageHistoryMapper imMessageHistoryMapper;
    private final ImMessageBodyMapper imMessageBodyMapper;
    private final ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Transactional
    public void doStoreP2PMessage(DoStoreP2PMessageDto doStoreP2PMessageDto) {
        imMessageBodyMapper.insert(doStoreP2PMessageDto.getImMessageBodyEntity());
        List<ImMessageHistoryEntity> imMessageHistoryEntities =
                extractTopP2PMessageHistory(doStoreP2PMessageDto.getMessageContent(),
                        doStoreP2PMessageDto.getImMessageBodyEntity());
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
    }

    public List<ImMessageHistoryEntity> extractTopP2PMessageHistory(MessageContent messageContent,
                                                                    ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());
        fromHistory.setSequence(messageContent.getMessageSequence());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());
        toHistory.setSequence(messageContent.getMessageSequence());

        list.add(fromHistory);
        list.add(toHistory);

        return list;
    }

    @Transactional
    public void doStoreGroupMessage(DoStoreGroupMessageDto doStoreGroupMessageDto) {
        imMessageBodyMapper.insert(doStoreGroupMessageDto.getImMessageBodyEntity());
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity =
                extractToGroupMessageHistory(doStoreGroupMessageDto.getGroupChatMessageContent(),
                        doStoreGroupMessageDto.getImMessageBodyEntity());
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent,
                                                                     ImMessageBodyEntity imMessageBodyEntity) {
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(imMessageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }
}
