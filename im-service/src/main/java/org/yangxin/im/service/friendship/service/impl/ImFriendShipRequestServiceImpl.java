package org.yangxin.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yangxin.im.codec.pack.friendship.ApproverFriendRequestPack;
import org.yangxin.im.codec.pack.friendship.ReadAllFriendRequestPack;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.ApproverFriendRequestStatusEnum;
import org.yangxin.im.common.enums.FriendShipErrorCode;
import org.yangxin.im.common.enums.command.FriendshipEventCommand;
import org.yangxin.im.common.exception.ApplicationException;
import org.yangxin.im.service.friendship.dao.ImFriendShipRequestEntity;
import org.yangxin.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import org.yangxin.im.service.friendship.model.req.ApproverFriendRequestReq;
import org.yangxin.im.service.friendship.model.req.FriendDto;
import org.yangxin.im.service.friendship.model.req.ReadFriendShipRequestReq;
import org.yangxin.im.service.friendship.service.ImFriendService;
import org.yangxin.im.service.friendship.service.ImFriendShipRequestService;
import org.yangxin.im.service.seq.RedisSeq;
import org.yangxin.im.service.util.MessageProducer;
import org.yangxin.im.service.util.WriteUserSeq;

import java.util.List;


@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "rawtypes", "unchecked"})
@Service
public class ImFriendShipRequestServiceImpl implements ImFriendShipRequestService {

    @Autowired
    ImFriendShipRequestMapper imFriendShipRequestMapper;

    @Autowired
    ImFriendService imFriendShipService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Override
    public ResponseVO getFriendRequest(String fromId, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper();
        query.eq("app_id", appId);
        query.eq("to_id", fromId);

        List<ImFriendShipRequestEntity> requestList = imFriendShipRequestMapper.selectList(query);

        return ResponseVO.successResponse(requestList);
    }


    //A + B
    @Override
    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("from_id", fromId);
        queryWrapper.eq("to_id", dto.getToId());
        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectOne(queryWrapper);

        Long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.FriendshipRequest);

        if (request == null) {
            request = new ImFriendShipRequestEntity();
            request.setAddSource(dto.getAddSource());
            request.setAddWording(dto.getAddWording());
            request.setSequence(seq);
            request.setAppId(appId);
            request.setFromId(fromId);
            request.setToId(dto.getToId());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(dto.getRemark());
            request.setCreateTime(System.currentTimeMillis());
            imFriendShipRequestMapper.insert(request);

        } else {
            //修改记录内容 和更新时间
            if (StringUtils.isNotBlank(dto.getAddSource())) request.setAddWording(dto.getAddWording());
            if (StringUtils.isNotBlank(dto.getRemark())) request.setRemark(dto.getRemark());
            if (StringUtils.isNotBlank(dto.getAddWording())) request.setAddWording(dto.getAddWording());
            request.setApproveStatus(0);
            request.setReadStatus(0);
            request.setSequence(seq);
            imFriendShipRequestMapper.updateById(request);
        }

        writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.FriendshipRequest, seq);

        // 发送好友申请的 tcp 给接收端
        messageProducer.sendToUser(dto.getToId(), null, "", FriendshipEventCommand.FRIEND_REQUEST, request, appId);

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req) {

        ImFriendShipRequestEntity imFriendShipRequestEntity = imFriendShipRequestMapper.selectById(req.getId());
        if (imFriendShipRequestEntity == null)
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);

        //只能审批发给自己的好友请求
        if (!req.getOperater().equals(imFriendShipRequestEntity.getToId()))
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);

        Long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipRequest);

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setApproveStatus(req.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update.setSequence(seq);
        update.setId(req.getId());
        imFriendShipRequestMapper.updateById(update);

        writeUserSeq.writeUserSeq(req.getAppId(), req.getOperater(), Constants.SeqConstants.FriendshipRequest, seq);

        if (ApproverFriendRequestStatusEnum.AGREE.getCode() == req.getStatus()) {
            //同意 ===> 去执行添加好友逻辑
            FriendDto dto = new FriendDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddWording(imFriendShipRequestEntity.getAddWording());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());
            ResponseVO responseVO = imFriendShipService.doAddFriend(req, imFriendShipRequestEntity.getFromId(), dto,
                    req.getAppId());
//            if(!responseVO.isOk()){
////                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return responseVO;
//            }
            if (!responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode())
                return responseVO;
        }

        ApproverFriendRequestPack approverFriendRequestPack = new ApproverFriendRequestPack();
        approverFriendRequestPack.setId(req.getId());
        approverFriendRequestPack.setSequence(seq);
        approverFriendRequestPack.setStatus(req.getStatus());
        messageProducer.sendToUser(imFriendShipRequestEntity.getToId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_REQUEST_APPROVER, approverFriendRequestPack, req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req) {
        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId());

        Long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipRequest);

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setReadStatus(1);
        update.setSequence(seq);
        imFriendShipRequestMapper.update(update, query);
        writeUserSeq.writeUserSeq(req.getAppId(), req.getOperater(), Constants.SeqConstants.FriendshipRequest, seq);

        // tcp 通知
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
        readAllFriendRequestPack.setSequence(seq);
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_REQUEST_READ, readAllFriendRequestPack, req.getAppId());

        return ResponseVO.successResponse();
    }

}
