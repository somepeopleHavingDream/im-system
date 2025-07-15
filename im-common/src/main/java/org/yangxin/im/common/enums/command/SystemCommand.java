package org.yangxin.im.common.enums.command;

public enum SystemCommand implements Command {
    /**
     * 心跳
     */
    PING(0x270f),
    /**
     * 登录
     */
    LOGIN(0x2328),
    /**
     * 登录 ack
     */
    LOGINACK(0X2329),
    /**
     * 登出
     */
    LOGOUT(0x232b),
    /**
     * 下线通知，用于多端互斥
     */
    MUTUALLOGIN(0X232a),
    ;

    private final int command;

    SystemCommand(int command) {
        this.command = command;
    }

    @Override
    public int getCommand() {
        return command;
    }
}
