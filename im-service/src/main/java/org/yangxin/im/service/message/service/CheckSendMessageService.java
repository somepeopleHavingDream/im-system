package org.yangxin.im.service.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.config.AppConfig;
import org.yangxin.im.common.enums.*;
import org.yangxin.im.service.friendship.dao.ImFriendShipEntity;
import org.yangxin.im.service.friendship.model.req.GetRelationReq;
import org.yangxin.im.service.friendship.service.ImFriendService;
import org.yangxin.im.service.group.dao.ImGroupEntity;
import org.yangxin.im.service.group.model.resp.GetRoleInGroupResp;
import org.yangxin.im.service.group.service.ImGroupMemberService;
import org.yangxin.im.service.group.service.ImGroupService;
import org.yangxin.im.service.user.dao.ImUserDataEntity;
import org.yangxin.im.service.user.service.ImUserService;

@SuppressWarnings({"unchecked", "DuplicatedCode"})
@Service
@RequiredArgsConstructor
public class CheckSendMessageService {
    private final ImUserService imUserService;
    private final ImFriendService imFriendService;
    private final ImGroupService imGroupService;
    private final ImGroupMemberService imGroupMemberService;

    private final AppConfig appConfig;

    public ResponseVO<?> checkSenderForbiddenAndMute(String fromId, Integer appId) {
        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        ImUserDataEntity user = singleUserInfo.getData();
        if (user.getForbiddenFlag() == UserForbiddenFlagEnum.FORBIBBEN.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
        } else if (user.getSilentFlag() == UserSilentFlagEnum.MUTE.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO<?> checkFriendShip(String fromId, String toId, Integer appId) {
        if (appConfig.isSendMessageCheckFriend()) {
            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setFromId(fromId);
            fromReq.setToId(toId);
            fromReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> fromRelation = imFriendService.getRelation(fromReq);
            if (!fromRelation.isOk()) {
                return fromRelation;
            }
            GetRelationReq toReq = new GetRelationReq();
            toReq.setFromId(toId);
            toReq.setToId(fromId);
            toReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> toRelation = imFriendService.getRelation(toReq);
            if (!toRelation.isOk()) {
                return toRelation;
            }

            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != fromRelation.getData().getStatus()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toRelation.getData().getStatus()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            if (appConfig.isSendMessageCheckBlack()) {
                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode() != fromRelation.getData().getBlack()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }

                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode() != toRelation.getData().getBlack()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO<?> checkGroupMessage(String fromId, String groupId, Integer appId) {
        ResponseVO<?> responseVO = checkSenderForbiddenAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }

        // 判断群逻辑
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(groupId, appId);
        if (!group.isOk()) {
            return group;
        }

        // 判断群成员是否在群内
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(groupId, fromId, appId);
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }
        GetRoleInGroupResp data = roleInGroupOne.getData();

        // 判断群是否被禁言
        // 如果禁言，只有群管理和群主可以发言
        ImGroupEntity groupData = group.getData();
        if (groupData.getMute() == GroupMuteTypeEnum.MUTE.getCode() && (data.getRole() == GroupMemberRoleEnum.MAMAGER.getCode() || data.getRole() == GroupMemberRoleEnum.OWNER.getCode())) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_GROUP_IS_MUTE);
        }

        if (data.getSpeakDate() != null && data.getSpeakDate() > System.currentTimeMillis()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }

        return ResponseVO.successResponse();
    }
}
