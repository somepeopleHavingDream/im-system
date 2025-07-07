package org.yangxin.im.service.group.service;

import org.yangxin.im.common.ResponseVO;
import org.yangxin.im.common.model.SyncReq;
import org.yangxin.im.service.group.dao.ImGroupEntity;
import org.yangxin.im.service.group.model.req.*;

@SuppressWarnings("rawtypes")
public interface ImGroupService {

    ResponseVO importGroup(ImportGroupReq req);

    ResponseVO createGroup(CreateGroupReq req);

    ResponseVO updateBaseGroupInfo(UpdateGroupReq req);

    ResponseVO getJoinedGroup(GetJoinedGroupReq req);

    ResponseVO destroyGroup(DestroyGroupReq req);

    ResponseVO transferGroup(TransferGroupReq req);

    ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);

    ResponseVO getGroup(GetGroupReq req);

    ResponseVO muteGroup(MuteGroupReq req);

    ResponseVO syncJoinedGroupList(SyncReq req);
}
