package org.yangxin.im.common.enums;

import lombok.Getter;

@Getter
public enum RouteHashMethodEnum {

    /**
     * TreeMap
     */
    TREE(1, "org.yangxin.im.common.route.algorithm.consistenthash.TreeMapConsistentHash"),

    /**
     * 自定义map
     */
    CUSTOMER(2, "com.lld.im.common.route.algorithm.consistenthash.xxxx"),

    ;


    private final int code;
    private final String clazz;

    RouteHashMethodEnum(int code, String clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     */
    public static RouteHashMethodEnum getHandler(int ordinal) {
        for (int i = 0; i < RouteHashMethodEnum.values().length; i++) {
            if (RouteHashMethodEnum.values()[i].getCode() == ordinal) {
                return RouteHashMethodEnum.values()[i];
            }
        }
        return null;
    }

}
