package org.yangxin.im.common.model;

import lombok.Data;

@Data
public class RequestBase {
    private Integer appId;

    private String operater;

    private Integer clientType;

    private String imei;
}
