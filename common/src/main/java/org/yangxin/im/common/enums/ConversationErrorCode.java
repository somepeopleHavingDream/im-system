package org.yangxin.im.common.enums;

import lombok.Getter;
import org.yangxin.im.common.exception.ApplicationExceptionEnum;

@Getter
public enum ConversationErrorCode implements ApplicationExceptionEnum {

    CONVERSATION_UPDATE_PARAM_ERROR(50000, "會話修改參數錯誤"),


    ;

    private final int code;
    private final String error;

    ConversationErrorCode(int code, String error) {
        this.code = code;
        this.error = error;
    }

}

