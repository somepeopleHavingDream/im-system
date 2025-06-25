package org.yangxin.im.common.enums.command;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageCommand implements Command {
    // 单聊消息 1103
    MSG_P2P(0x44F),
    // 单聊消息 ack 1046
    MSG_ACK(0x416),
    // 消息收到 ack 1107
    MSG_RECEIVE_ACK(1107),
    ;

    private final int command;

    @Override
    public int getCommand() {
        return command;
    }
}
