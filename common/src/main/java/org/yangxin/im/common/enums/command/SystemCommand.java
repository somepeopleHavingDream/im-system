package org.yangxin.im.common.enums.command;

public enum SystemCommand implements Command {
    /**
     * 登录
     */
    LOGIN(0x2328);

    private final int command;

    SystemCommand(int command) {
        this.command = command;
    }

    @Override
    public int getCommand() {
        return command;
    }
}
