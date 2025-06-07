package org.yangxin.im.service.user.model.req;

import org.yangxin.im.common.model.RequestBase;
import org.yangxin.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;


@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;


}
