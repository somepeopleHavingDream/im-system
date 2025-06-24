package org.yangxin.im.tcp.feign;

import feign.Headers;
import feign.RequestLine;
import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.model.CheckSendMessageReq;

public interface FeignMessageService {
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    ResponseVO<?> checkSendMessage(CheckSendMessageReq o);
}
