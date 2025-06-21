package org.yangxin.im.common.enums.command;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserEventCommand implements Command {
    USER_MODIFY(4000),
    ;

    private final int command;

    @Override
    public int getCommand() {
        return command;
    }
}
