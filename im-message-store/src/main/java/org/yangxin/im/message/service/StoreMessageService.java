package org.yangxin.im.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yangxin.im.common.model.message.MessageContent;
import org.yangxin.im.message.dao.ImMessageBodyEntity;
import org.yangxin.im.message.dao.ImMessageHistoryEntity;
import org.yangxin.im.message.dao.mapper.ImMessageBodyMapper;
import org.yangxin.im.message.dao.mapper.ImMessageHistoryMapper;
import org.yangxin.im.message.model.DoStoreP2PMessageDto;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreMessageService {
    private final ImMessageHistoryMapper imMessageHistoryMapper;
    private final ImMessageBodyMapper imMessageBodyMapper;

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
