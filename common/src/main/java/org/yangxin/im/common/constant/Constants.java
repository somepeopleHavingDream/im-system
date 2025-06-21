package org.yangxin.im.common.constant;

public class Constants {
    public static final String UserId = "userId";
    public static final String AppId = "appId";
    public static final String ClientType = "clientType";
    public static final String Imei = "imei";
    public static final String ReadTime = "readTime";
    public static final String ImCoreZkRoot = "/im-coreRoot";
    public static final String ImCoreZkRootTcp = "/tcp";
    public static final String ImCoreZkRootWeb = "/web";

    public static class RedisConstants {
        public static final String userSign = "userSign";
        public static final String UserLoginChannel = "signal/channel/LOGIN_USER_INNER_QUEUE";
        public static final String UserSessionConstants = ":userSession:";
    }

    public static class RabbitConstants {

        public static final String Im2UserService = "pipeline2UserService";

        public static final String Im2MessageService = "pipeline2MessageService";

        public static final String Im2GroupService = "pipeline2GroupService";

        public static final String Im2FriendshipService = "pipeline2FriendshipService";

        public static final String MessageService2Im = "messageService2Pipeline";

        public static final String GroupService2Im = "GroupService2Pipeline";

        public static final String FriendShip2Im = "friendShip2Pipeline";

        public static final String StoreP2PMessage = "storeP2PMessage";

        public static final String StoreGroupMessage = "storeGroupMessage";
    }

    public static class CallbackCommand {
        public static final String ModifyUserAfter = "user.modify.after";

        public static final String CreateGroupAfter = "group.create.after";

        public static final String UpdateGroupAfter = "group.update.after";

        public static final String DestoryGroupAfter = "group.destory.after";

        public static final String TransferGroupAfter = "group.transfer.after";

        public static final String GroupMemberAddBefore = "group.member.add.before";

        public static final String GroupMemberAddAfter = "group.member.add.after";

        public static final String GroupMemberDeleteAfter = "group.member.delete.after";

        public static final String AddFriendBefore = "friend.add.before";

        public static final String AddFriendAfter = "friend.add.after";

        public static final String UpdateFriendBefore = "friend.update.before";

        public static final String UpdateFriendAfter = "friend.update.after";

        public static final String DeleteFriendAfter = "friend.delete.after";

        public static final String AddBlackAfter = "black.add.after";

        public static final String DeleteBlack = "black.delete";

        public static final String SendMessageAfter = "message.send.after";

        public static final String SendMessageBefore = "message.send.before";

    }
}
