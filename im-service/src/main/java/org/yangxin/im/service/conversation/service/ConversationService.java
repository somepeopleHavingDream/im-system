package org.yangxin.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yangxin.im.codec.pack.conversation.DeleteConversationPack;
import org.yangxin.im.codec.pack.conversation.UpdateConversationPack;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.config.AppConfig;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.ConversationErrorCode;
import org.yangxin.im.common.enums.ConversationTypeEnum;
import org.yangxin.im.common.enums.command.ConversationEventCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.SyncReq;
import org.yangxin.im.common.model.SyncResp;
import org.yangxin.im.common.model.message.MessageReadedContent;
import org.yangxin.im.service.conversation.dao.ImConversationSetEntity;
import org.yangxin.im.service.conversation.dao.mapper.ImConversationSetMapper;
import org.yangxin.im.service.conversation.model.DeleteConversationReq;
import org.yangxin.im.service.conversation.model.UpdateConversationReq;
import org.yangxin.im.service.seq.RedisSeq;
import org.yangxin.im.service.util.MessageProducer;
import org.yangxin.im.service.util.WriteUserSeq;

import java.util.List;

@SuppressWarnings("rawtypes")
@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ImConversationSetMapper imConversationSetMapper;
    private final MessageProducer messageProducer;
    private final AppConfig appConfig;
    private final RedisSeq redisSeq;
    private final WriteUserSeq writeUserSeq;

    public String convertConversationId(Integer type, String fromId, String toId) {
        return type + "_" + fromId + "_" + toId;
    }

    public void messageMarkRead(MessageReadedContent messageReadedContent) {
        String toId = messageReadedContent.getToId();
        if (messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()) {
            toId = messageReadedContent.getGroupId();
        }
        String conversationId = convertConversationId(messageReadedContent.getConversationType(),
                messageReadedContent.getFromId(), toId);

        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        query.eq("app_id", messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if (imConversationSetEntity == null) {
            imConversationSetEntity = new ImConversationSetEntity();
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent, imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(),
                    messageReadedContent.getFromId(), Constants.SeqConstants.Conversation, seq);
        } else {
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setSequence(seq);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(),
                    messageReadedContent.getFromId(), Constants.SeqConstants.Conversation, seq);
        }
    }

    public ResponseVO<?> deleteConversation(DeleteConversationReq req) {
        if (appConfig.getDeleteConversationSyncMode() == 1) {
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(), ConversationEventCommand.CONVERSATION_DELETE,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }
        return ResponseVO.successResponse();
    }

    public ResponseVO<?> updateConversation(UpdateConversationReq req) {
        if (req.getIsTop() == null && req.getIsMute() == null) {
            // 返回失败
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", req.getConversationId());
        queryWrapper.eq("app_id", req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity != null) {
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);
            if (req.getIsTop() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(),
                    Constants.SeqConstants.Conversation, seq);

            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(), ConversationEventCommand.CONVERSATION_UPDATE,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO syncConversationSet(SyncReq req) {
        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }

        SyncResp<ImConversationSetEntity> resp = new SyncResp<>();
        //seq > req.getseq limit maxLimit
        QueryWrapper<ImConversationSetEntity> queryWrapper =
                new QueryWrapper<>();
        queryWrapper.eq("from_id", req.getOperater());
        queryWrapper.gt("sequence", req.getLastSequence());
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("sequence");
        List<ImConversationSetEntity> list = imConversationSetMapper
                .selectList(queryWrapper);

        if (!CollectionUtils.isEmpty(list)) {
            ImConversationSetEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }
}
