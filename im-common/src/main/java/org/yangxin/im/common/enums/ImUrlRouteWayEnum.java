package org.yangxin.im.common.enums;

import lombok.Getter;

@Getter
public enum ImUrlRouteWayEnum {
    /**
     * 随机
     */
    RAMDOM(1, "org.yangxin.im.common.route.algorithm.random.RandomHandle"),

    /**
     * 1.轮训
     */
    LOOP(2, "org.yangxin.im.common.route.algorithm.loop.LoopHandle"),

    /**
     * HASH
     */
    HASH(3, "org.yangxin.im.common.route.algorithm.consistenthash.ConsistentHashHandle"),
    ;


    private final int code;
    private final String clazz;

    ImUrlRouteWayEnum(int code, String clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     */
    public static ImUrlRouteWayEnum getHandler(int ordinal) {
        for (int i = 0; i < ImUrlRouteWayEnum.values().length; i++) {
            if (ImUrlRouteWayEnum.values()[i].getCode() == ordinal) {
                return ImUrlRouteWayEnum.values()[i];
            }
        }
        return null;
    }

}
