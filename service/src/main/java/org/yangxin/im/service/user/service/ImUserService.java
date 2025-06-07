package org.yangxin.im.service.user.service;

import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.service.user.dao.ImUserDataEntity;
import org.yangxin.im.service.user.model.req.DeleteUserReq;
import org.yangxin.im.service.user.model.req.GetUserInfoReq;
import org.yangxin.im.service.user.model.req.ImportUserReq;
import org.yangxin.im.service.user.model.req.ModifyUserInfoReq;
import org.yangxin.im.service.user.model.resp.GetUserInfoResp;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public interface ImUserService {

    public ResponseVO importUser(ImportUserReq req);

    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId);

    public ResponseVO deleteUser(DeleteUserReq req);

    public ResponseVO modifyUserInfo(ModifyUserInfoReq req);


}
