package org.yangxin.im.service.user.model.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.yangxin.im.common.model.RequestBase;

@EqualsAndHashCode(callSuper = true)
@Data
public class SetUserCustomerStatusReq extends RequestBase {

    private String userId;

    private String customText;

    private Integer customStatus;

}
