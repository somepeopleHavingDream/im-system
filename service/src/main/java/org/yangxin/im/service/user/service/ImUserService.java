package org.yangxin.im.service.user.service;

import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.service.user.dao.ImUserDataEntity;
import org.yangxin.im.service.user.model.req.*;
import org.yangxin.im.service.user.model.resp.GetUserInfoResp;

@SuppressWarnings("rawtypes")
public interface ImUserService {
    ResponseVO importUser(ImportUserReq req);

    ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId);

    ResponseVO deleteUser(DeleteUserReq req);

    ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    ResponseVO login(LoginReq req);

    ResponseVO getUserSequence(GetUserSequenceReq req);
}
