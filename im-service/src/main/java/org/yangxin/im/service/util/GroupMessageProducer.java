package org.yangxin.im.service.util;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yangxin.im.codec.pack.group.AddGroupMemberPack;
import org.yangxin.im.codec.pack.group.RemoveGroupMemberPack;
import org.yangxin.im.codec.pack.group.UpdateGroupMemberPack;
import org.yangxin.im.common.ClientType;
import org.yangxin.im.common.enums.command.Command;
import org.yangxin.im.common.enums.command.GroupEventCommand;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.service.group.model.req.GroupMemberDto;
import org.yangxin.im.service.group.service.ImGroupMemberService;

import java.util.List;

@SuppressWarnings("EqualsBetweenInconvertibleTypes")
@Component
@RequiredArgsConstructor
public class GroupMessageProducer {
    private final MessageProducer messageProducer;
    private final ImGroupMemberService imGroupMemberService;

    public void producer(String userId, Command command, Object data, ClientInfo clientInfo) {
        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        String groupId = o.getString("groupId");
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());

        if (command.equals(GroupEventCommand.ADDED_MEMBER)) {
            //发送给管理员和被加入人本身
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            AddGroupMemberPack addGroupMemberPack
                    = o.toJavaObject(AddGroupMemberPack.class);
            List<String> members = addGroupMemberPack.getMembers();
            for (GroupMemberDto groupMemberDto : groupManager) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && groupMemberDto.getMemberId().equals(userId)) {
                    messageProducer.sendToUserExceptClient(groupMemberDto.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(groupMemberDto.getMemberId(), command, data, clientInfo.getAppId());
                }
            }
            for (String member : members) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    messageProducer.sendToUserExceptClient(member, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(member, command, data, clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.DELETED_MEMBER)) {
            RemoveGroupMemberPack pack = o.toJavaObject(RemoveGroupMemberPack.class);
            String member = pack.getMember();
            List<String> members = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
            members.add(member);
            for (String memberId : members) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.UPDATED_MEMBER)) {
            UpdateGroupMemberPack pack =
                    o.toJavaObject(UpdateGroupMemberPack.class);
            String memberId = pack.getMemberId();
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            GroupMemberDto groupMemberDto = new GroupMemberDto();
            groupMemberDto.setMemberId(memberId);
            groupManager.add(groupMemberDto);
            for (GroupMemberDto member : groupManager) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    messageProducer.sendToUserExceptClient(member.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(member.getMemberId(), command, data, clientInfo.getAppId());
                }
            }
        } else {
            for (String memberId : groupMemberId) {
                if (clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() && memberId.equals(userId)) {
                    messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(memberId, command, data, clientInfo);
                }
            }
        }
    }
}
