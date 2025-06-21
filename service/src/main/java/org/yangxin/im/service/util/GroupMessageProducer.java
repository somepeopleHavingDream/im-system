package org.yangxin.im.service.util;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yangxin.im.common.ClientType;
import org.yangxin.im.common.enums.command.Command;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.service.group.service.ImGroupMemberService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupMessageProducer {
    private final MessageProducer messageProducer;
    private final ImGroupMemberService imGroupMemberService;

    public void producer(String userId, Command command, Object data, ClientInfo clientInfo) {
        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        String groupId = o.getString("groupId");
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
        for (String memberId : groupMemberId) {
            if (clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() && memberId.equals(userId)) {
                messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
            } else {
                messageProducer.sendToUser(memberId, command, data, clientInfo);
            }
        }
    }
}
