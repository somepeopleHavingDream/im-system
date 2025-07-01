package org.yangxin.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.pack.conversation.DeleteConversationPack;
import org.yangxin.im.codec.pack.conversation.UpdateConversationPack;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.config.AppConfig;
import org.yangxin.im.common.enums.ConversationErrorCode;
import org.yangxin.im.common.enums.ConversationTypeEnum;
import org.yangxin.im.common.enums.command.ConversationEventCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.message.MessageReadedContent;
import org.yangxin.im.service.conversation.dao.ImConversationSetEntity;
import org.yangxin.im.service.conversation.dao.mapper.ImConversationSetMapper;
import org.yangxin.im.service.conversation.model.DeleteConversationReq;
import org.yangxin.im.service.conversation.model.UpdateConversationReq;
import org.yangxin.im.service.util.MessageProducer;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ImConversationSetMapper imConversationSetMapper;
    private final MessageProducer messageProducer;
    private final AppConfig appConfig;

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
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent, imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.insert(imConversationSetEntity);
        } else {
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
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
            if (req.getIsTop() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);

            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(), ConversationEventCommand.CONVERSATION_UPDATE,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }

        return ResponseVO.successResponse();
    }
}
