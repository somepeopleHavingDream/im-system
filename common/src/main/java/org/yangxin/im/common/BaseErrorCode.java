package org.yangxin.im.common;

import org.yangxin.im.common.exception.ApplicationExceptionEnum;

public enum BaseErrorCode implements ApplicationExceptionEnum {

    SUCCESS(200, "success"),
    SYSTEM_ERROR(90000, "服务器内部错误,请联系管理员"),
    PARAMETER_ERROR(90001, "参数校验错误"),


    ;

    private final int code;
    private final String error;

    BaseErrorCode(int code, String error) {
        this.code = code;
        this.error = error;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getError() {
        return error;
    }

}
