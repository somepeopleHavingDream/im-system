package org.yangxin.im.common.enums;

import lombok.Getter;

@Getter
public enum ImConnectStatusEnum {

    /**
     * 管道链接状态,1=在线，2=离线。。
     */
    ONLINE_STATUS(1),

    OFFLINE_STATUS(2),
    ;

    private final Integer code;

    ImConnectStatusEnum(Integer code) {
        this.code = code;
    }

}
