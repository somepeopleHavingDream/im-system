package org.yangxin.im.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncReq extends RequestBase {

    //客户端最大seq
    private Long lastSequence;
    //一次拉取多少
    private Integer maxLimit;

}
