package org.yangxin.im.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yangxin.im.common.exception.ApplicationExceptionEnum;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseVO<T> {

    private int code;

    private String msg;

    private T data;

    public ResponseVO(int code, String msg) {
        this.code = code;
        this.msg = msg;
//		this.data = null;
    }

    public static ResponseVO successResponse(Object data) {
        return new ResponseVO(200, "success", data);
    }

    public static ResponseVO successResponse() {
        return new ResponseVO(200, "success");
    }

    public static ResponseVO errorResponse() {
        return new ResponseVO(500, "系统内部异常");
    }

    public static ResponseVO errorResponse(int code, String msg) {
        return new ResponseVO(code, msg);
    }

    public static ResponseVO errorResponse(ApplicationExceptionEnum enums) {
        return new ResponseVO(enums.getCode(), enums.getError());
    }

    public boolean isOk() {
        return code == 200;
    }

    public ResponseVO success() {
        code = 200;
        msg = "success";
        return this;
    }

    public ResponseVO success(T data) {
        code = 200;
        msg = "success";
        this.data = data;
        return this;
    }

}
