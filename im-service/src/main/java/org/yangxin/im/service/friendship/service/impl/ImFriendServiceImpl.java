package org.yangxin.im.service.friendship.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yangxin.im.codec.pack.friendship.*;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.config.AppConfig;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.AllowFriendTypeEnum;
import org.yangxin.im.common.enums.CheckFriendShipTypeEnum;
import org.yangxin.im.common.enums.FriendShipErrorCode;
import org.yangxin.im.common.enums.FriendShipStatusEnum;
import org.yangxin.im.common.enums.command.FriendshipEventCommand;
import org.yangxin.im.common.exception.ApplicationException;
import org.yangxin.im.common.model.RequestBase;
import org.yangxin.im.common.model.SyncReq;
import org.yangxin.im.common.model.SyncResp;
import org.yangxin.im.service.friendship.dao.ImFriendShipEntity;
import org.yangxin.im.service.friendship.dao.mapper.ImFriendShipMapper;
import org.yangxin.im.service.friendship.model.callback.AddFriendAfterCallbackDto;
import org.yangxin.im.service.friendship.model.callback.AddFriendBlackAfterCallbackDto;
import org.yangxin.im.service.friendship.model.callback.DeleteFriendAfterCallbackDto;
import org.yangxin.im.service.friendship.model.req.*;
import org.yangxin.im.service.friendship.model.resp.CheckFriendShipResp;
import org.yangxin.im.service.friendship.model.resp.ImportFriendShipResp;
import org.yangxin.im.service.friendship.service.ImFriendService;
import org.yangxin.im.service.friendship.service.ImFriendShipRequestService;
import org.yangxin.im.service.seq.RedisSeq;
import org.yangxin.im.service.user.dao.ImUserDataEntity;
import org.yangxin.im.service.user.service.ImUserService;
import org.yangxin.im.service.util.CallbackService;
import org.yangxin.im.service.util.MessageProducer;
import org.yangxin.im.service.util.WriteUserSeq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "rawtypes", "CallToPrintStackTrace", "DuplicatedCode"
        , "SpringTransactionalMethodCallsInspection", "unchecked"})
@Service
public class ImFriendServiceImpl implements ImFriendService {

    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendService imFriendService;

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Override
    public ResponseVO importFriendShip(ImporFriendShipReq req) {

        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImporFriendShipReq.ImportFriendDto dto :
                req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto, entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if (insert == 1) {
                    successId.add(dto.getToId());
                } else {
                    errorId.add(dto.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorId.add(dto.getToId());
            }

        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriend(AddFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(),
                req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        // 之前回调
        if (appConfig.isAddFriendBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(),
                    Constants.CallbackCommand.AddFriendBefore, JSONObject.toJSONString(req));
            if (!responseVO.isOk()) {
                return responseVO;
            }
        }

        ImUserDataEntity data = toInfo.getData();

        if (data.getFriendAllowType() != null && data.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()) {
            return doAddFriend(req, req.getFromId(), req.getToItem(), req.getAppId());
        } else {
            QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("from_id", req.getFromId());
            query.eq("to_id", req.getToItem().getToId());
            ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
            if (fromItem == null || fromItem.getStatus()
                    != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                //插入一条好友申请的数据
                ResponseVO responseVO = imFriendShipRequestService.addFienshipRequest(req.getFromId(),
                        req.getToItem(), req.getAppId());
                if (!responseVO.isOk()) {
                    return responseVO;
                }
            } else {
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }

        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(),
                req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        ResponseVO responseVO = doUpdate(req.getFromId(), req.getToItem(), req.getAppId());
        if (responseVO.isOk()) {
            UpdateFriendPack updateFriendPack = new UpdateFriendPack();
            updateFriendPack.setRemark(req.getToItem().getRemark());
            updateFriendPack.setToId(req.getToItem().getToId());
            messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                    FriendshipEventCommand.FRIEND_UPDATE, updateFriendPack, req.getAppId());
            // 之后回调
            if (appConfig.isModifyFriendAfterCallback()) {
                AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToItem(req.getToItem());
                callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.UpdateFriendAfter,
                        JSONObject.toJSONString(callbackDto));
            }
        }
        return responseVO;
    }

    @Transactional
    public ResponseVO doUpdate(String fromId, FriendDto dto, Integer appId) {
        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);
        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getAddSource, dto.getAddSource())
                .set(ImFriendShipEntity::getExtra, dto.getExtra())
                .set(ImFriendShipEntity::getFriendSequence, seq)
                .set(ImFriendShipEntity::getRemark, dto.getRemark())
                .eq(ImFriendShipEntity::getAppId, appId)
                .eq(ImFriendShipEntity::getToId, dto.getToId())
                .eq(ImFriendShipEntity::getFromId, fromId);

        int update = imFriendShipMapper.update(null, updateWrapper);
        if (update == 1) {
            writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);
            return ResponseVO.successResponse();
        }

        return ResponseVO.errorResponse();
    }

    @Override
    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId) {

        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在，如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("to_id", dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        //如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态
        long seq;
        if (fromItem == null) {
            //走添加逻辑。
            fromItem = new ImFriendShipEntity();
            seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);
            fromItem.setAppId(appId);
            fromItem.setFriendSequence(seq);
            fromItem.setFromId(fromId);
//            entity.setToId(to);
            BeanUtils.copyProperties(dto, fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);
        } else if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
        } else {
            ImFriendShipEntity update = new ImFriendShipEntity();

            if (StringUtils.isNotBlank(dto.getAddSource())) {
                update.setAddSource(dto.getAddSource());
            }

            if (StringUtils.isNotBlank(dto.getRemark())) {
                update.setRemark(dto.getRemark());
            }

            if (StringUtils.isNotBlank(dto.getExtra())) {
                update.setExtra(dto.getExtra());
            }
            seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);
            update.setFriendSequence(seq);
            update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

            int result = imFriendShipMapper.update(update, query);
            if (result != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);
        }

        QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("app_id", appId);
        toQuery.eq("from_id", dto.getToId());
        toQuery.eq("to_id", fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);
        if (toItem == null) {
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            BeanUtils.copyProperties(dto, toItem);
            toItem.setToId(fromId);
            toItem.setFriendSequence(seq);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
//            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            int insert = imFriendShipMapper.insert(toItem);
            writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.Friendship, seq);
        } else if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() !=
                toItem.getStatus()) {
            ImFriendShipEntity update = new ImFriendShipEntity();
            update.setFriendSequence(seq);
            update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            imFriendShipMapper.update(update, toQuery);
            writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.Friendship, seq);
        }

        // 发送给 from
        AddFriendPack addFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(fromItem, addFriendPack);
        addFriendPack.setSequence(seq);
        if (requestBase != null) {
            messageProducer.sendToUser(fromId, requestBase.getClientType(), requestBase.getImei(),
                    FriendshipEventCommand.FRIEND_ADD, addFriendPack, requestBase.getAppId());
        } else {
            messageProducer.sendToUser(fromId, FriendshipEventCommand.FRIEND_ADD, addFriendPack,
                    requestBase.getAppId());
        }

        AddFriendPack addFriendToPack = new AddFriendPack();
        BeanUtils.copyProperties(toItem, addFriendToPack);
        messageProducer.sendToUser(toItem.getFromId(), FriendshipEventCommand.FRIEND_ADD, addFriendToPack,
                requestBase.getAppId());

        // 之后回调
        if (appConfig.isAddFriendAfterCallback()) {
            AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
            callbackDto.setFromId(fromId);
            callbackDto.setToItem(dto);
            callbackService.beforeCallback(appId, Constants.CallbackCommand.AddFriendAfter,
                    JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }


    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
            if (fromItem.getStatus() != null && fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                ImFriendShipEntity update = new ImFriendShipEntity();
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
                imFriendShipMapper.update(update, query);
                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);
                DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
                deleteFriendPack.setFromId(req.getFromId());
                deleteFriendPack.setSequence(seq);
                deleteFriendPack.setToId(req.getToId());
                messageProducer.sendToUser(req.getFromId(),
                        req.getClientType(), req.getImei(),
                        FriendshipEventCommand.FRIEND_DELETE,
                        deleteFriendPack, req.getAppId());

                //之后回调
                if (appConfig.isAddFriendAfterCallback()) {
                    DeleteFriendAfterCallbackDto callbackDto = new DeleteFriendAfterCallbackDto();
                    callbackDto.setFromId(req.getFromId());
                    callbackDto.setToId(req.getToId());
                    callbackService.beforeCallback(req.getAppId(),
                            Constants.CallbackCommand.DeleteFriendAfter, JSONObject
                                    .toJSONString(callbackDto));
                }

            } else {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("status", FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(update, query);

        DeleteAllFriendPack deleteAllFriendPack = new DeleteAllFriendPack();
        deleteAllFriendPack.setFromId(req.getFromId());
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_DELETE, deleteAllFriendPack, req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        return ResponseVO.successResponse(imFriendShipMapper.selectList(query));
    }

    @Override
    public ResponseVO getRelation(GetRelationReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity entity = imFriendShipMapper.selectOne(query);
        if (entity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO checkBlck(CheckFriendShipReq req) {

        Map<String, Integer> toIdMap
                = req.getToIds().stream().collect(Collectors
                .toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResp> result;
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            result = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            result = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream()
                .collect(Collectors
                        .toMap(CheckFriendShipResp::getToId,
                                CheckFriendShipResp::getStatus));
        for (String toId :
                toIdMap.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                result.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(result);
    }

    @Override
    public ResponseVO syncFriendshipList(SyncReq req) {
        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }

        SyncResp<ImFriendShipEntity> resp = new SyncResp<>();
        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id", req.getOperater());
        queryWrapper.gt("friend_sequence", req.getLastSequence());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("friend_sequence");
        List<ImFriendShipEntity> list = imFriendShipMapper.selectList(queryWrapper);

        if (!CollectionUtils.isEmpty(list)) {
            ImFriendShipEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            // 设置最大 seq
            Long friendShipMaxSeq = imFriendShipMapper.getFriendShipMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            // 设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getFriendSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public List<String> getAllFriendId(String userId, Integer appId) {
        return imFriendShipMapper.getAllFriendId(userId, appId);
    }


    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
        Long seq;
        if (fromItem == null) {
            //走添加逻辑。
            seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);

            fromItem = new ImFriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setFriendSequence(seq);
            fromItem.setAppId(req.getAppId());
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);
        } else if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
        } else {
            seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);
            ImFriendShipEntity update = new ImFriendShipEntity();
            update.setFriendSequence(seq);
            update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            int result = imFriendShipMapper.update(update, query);
            if (result != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
            }
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);
        }

        AddFriendBlackPack addFriendBlackPack = new AddFriendBlackPack();
        addFriendBlackPack.setFromId(req.getFromId());
        addFriendBlackPack.setSequence(seq);
        addFriendBlackPack.setToId(req.getToId());
        // 发送 tcp 通知
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_BLACK_ADD, addFriendBlackPack, req.getAppId());
        // 之后回调
        if (appConfig.isAddFriendShipBlackAfterCallback()) {
            AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
            callbackDto.setFromId(fromItem.getFromId());
            callbackDto.setToId(fromItem.getToId());
            callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.AddBlackAfter,
                    JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", req.getFromId())
                .eq("app_id", req.getAppId())
                .eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);
        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }
        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        int update1 = imFriendShipMapper.update(update, queryFrom);
        if (update1 == 1) {
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);
            DeleteBlackPack deleteBlackPack = new DeleteBlackPack();
            deleteBlackPack.setFromId(req.getFromId());
            deleteBlackPack.setSequence(seq);
            deleteBlackPack.setToId(req.getToId());
            messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                    FriendshipEventCommand.FRIEND_BLACK_DELETE, deleteBlackPack, req.getAppId());
            // 之后回调
            if (appConfig.isAddFriendShipBlackAfterCallback()) {
                AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToId(req.getToId());
                callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.DeleteBlack,
                        JSONObject.toJSONString(callbackDto));
            }
            return ResponseVO.successResponse();
        }
        return ResponseVO.errorResponse();
    }

    @Override
    public ResponseVO checkFriendship(CheckFriendShipReq req) {

        Map<String, Integer> result
                = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp;

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            resp = imFriendShipMapper.checkFriendShip(req);
        } else {
            resp = imFriendShipMapper.checkFriendShipBoth(req);
        }

        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendShipResp::getToId
                        , CheckFriendShipResp::getStatus));

        for (String toId : result.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(result.get(toId));
                resp.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(resp);
    }

}
