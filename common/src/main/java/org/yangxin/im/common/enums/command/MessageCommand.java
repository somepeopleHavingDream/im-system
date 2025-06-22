package org.yangxin.im.common.enums.command;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageCommand implements Command {
    // 单聊消息 1103
    MSG_P2P(0x44F),
    ;

    private final int command;

    @Override
    public int getCommand() {
        return command;
    }
}
