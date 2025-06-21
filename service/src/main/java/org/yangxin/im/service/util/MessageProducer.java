package org.yangxin.im.service.util;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.yangxin.im.codec.proto.MessagePack;
import org.yangxin.im.common.enums.command.Command;
import org.yangxin.im.common.model.ClientInfo;
import org.yangxin.im.common.model.UserSession;

import java.util.List;
import java.util.Objects;

@SuppressWarnings({"rawtypes", "unchecked"})
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {
    private final RabbitTemplate rabbitTemplate;
    private final UserSessionUtil userSessionUtil;

    public boolean sendMessage(UserSession session, Object msg) {
        try {
            log.info("sendMessage {}", msg);
            rabbitTemplate.convertAndSend("", session.getBrokerId() + "", msg);
            return true;
        } catch (Exception e) {
            log.error("sendMessage error", e);
            return false;
        }
    }

    // 包装数据，调用 sendMessage
    public boolean sendPack(String toId, Command command, Object msg, UserSession session) {
        MessagePack messagePack = new MessagePack();
        messagePack.setCommand(command.getCommand());
        messagePack.setToId(toId);
        messagePack.setClientType(session.getClientType());
        messagePack.setAppId(session.getAppId());
        messagePack.setImei(session.getImei());

        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(msg));
        messagePack.setData(jsonObject);

        String body = JSONObject.toJSONString(messagePack);
        return sendMessage(session, body);
    }

    // 发送给所有端的方法
    public void sendToUser(String toId, Command command, Object data, Integer appId) {
        List<UserSession> sessionList = userSessionUtil.getUserSessions(appId, toId);
        for (UserSession session : sessionList) {
            sendPack(toId, command, data, session);
        }
    }

    // 发送给某个用户的指定客户端
    public void sendToUser(String toId, Command command, Object data, ClientInfo clientInfo) {
        UserSession userSessions = userSessionUtil.getUserSessions(clientInfo.getAppId(), toId, clientInfo.getClientType(), clientInfo.getImei());
        sendPack(toId, command, data, userSessions);
    }

    // 发送给除了某一端的其他端
    public void sendToUserExceptClient(String toId, Command command, Object data, ClientInfo clientInfo) {
        List<UserSession> sessionList = userSessionUtil.getUserSessions(clientInfo.getAppId(), toId);
        for (UserSession session : sessionList) {
            if (!isMatch(session, clientInfo)) {
                sendPack(toId, command, data, session);
            }
        }
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

}
