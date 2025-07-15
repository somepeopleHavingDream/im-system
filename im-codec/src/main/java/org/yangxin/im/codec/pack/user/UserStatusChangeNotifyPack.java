package org.yangxin.im.codec.pack.user;

import lombok.Data;
import org.yangxin.im.common.model.UserSession;

import java.util.List;

@Data
public class UserStatusChangeNotifyPack {
    private Integer appId;
    private String userId;
    private Integer status;
    private List<UserSession> client;
}
