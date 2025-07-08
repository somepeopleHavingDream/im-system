package org.yangxin.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yangxin.im.codec.pack.message.MessageReadedPack;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.command.Command;
import org.yangxin.im.common.enums.command.GroupEventCommand;
import org.yangxin.im.common.enums.command.MessageCommand;
import org.yangxin.im.common.model.SyncReq;
import org.yangxin.im.common.model.SyncResp;
import org.yangxin.im.common.model.message.MessageReadedContent;
import org.yangxin.im.common.model.message.MessageReceiveAckContent;
import org.yangxin.im.common.model.message.OfflineMessageContent;
import org.yangxin.im.service.conversation.service.ConversationService;
import org.yangxin.im.service.util.MessageProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked", "DataFlowIssue"})
@Service
@RequiredArgsConstructor
public class MessageSyncService {
    private final MessageProducer messageProducer;
    private final ConversationService conversationService;
    private final RedisTemplate redisTemplate;

    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {
        messageProducer.sendToUser(messageReceiveAckContent.getToId(), MessageCommand.MSG_RECEIVE_ACK,
                messageReceiveAckContent, messageReceiveAckContent.getAppId());
    }

    public void readMark(MessageReadedContent messageContent) {
        /*
            消息已读，更新会话的 seq ，通知在线的同步端发送指定 command ，发送已读回执通知
         */
        conversationService.messageMarkRead(messageContent);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageContent, messageReadedPack);
        syncToSender(messageReadedPack, messageContent, MessageCommand.MSG_READED_NOTIFY);
        // 发送给对方
        messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_READED_RECEIPT, messageReadedPack,
                messageContent.getAppId());
    }

    private void syncToSender(MessageReadedPack pack, MessageReadedContent content, Command command) {
        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getFromId(), command, pack, content);
    }

    public void groupReadMark(MessageReadedContent messageReaded) {
        conversationService.messageMarkRead(messageReaded);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded, messageReadedPack);
        syncToSender(messageReadedPack, messageReaded, GroupEventCommand.MSG_GROUP_READED_NOTIFY);
        messageProducer.sendToUser(messageReadedPack.getToId(), GroupEventCommand.MSG_GROUP_READED_RECEIPT,
                messageReaded, messageReaded.getAppId());
    }

    public ResponseVO syncOfflineMessage(SyncReq req) {
        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperater();
        //获取最大的seq
        long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if (!CollectionUtils.isEmpty(set)) {
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = o.getScore().longValue();
        }

        List<OfflineMessageContent> respList = new ArrayList<>();
        resp.setMaxSequence(maxSeq);

        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key,
                req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        if (!CollectionUtils.isEmpty(respList)) {
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }
}
