package org.yangxin.im.common.enums.command;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConversationEventCommand implements Command {
    // 删除会话
    CONVERSATION_DELETE(5000),
    // 更新会话
    CONVERSATION_UPDATE(5001),
    ;

    private final int command;

    @Override
    public int getCommand() {
        return command;
    }
}
