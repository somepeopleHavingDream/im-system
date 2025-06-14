package org.yangxin.im.common.enums;

public enum ApproverFriendRequestStatusEnum {

    /**
     * 1 同意；2 拒绝。
     */
    AGREE(1),

    REJECT(2),
    ;

    private final int code;

    ApproverFriendRequestStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
