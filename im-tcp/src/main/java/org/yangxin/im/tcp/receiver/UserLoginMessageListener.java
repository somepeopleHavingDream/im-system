package org.yangxin.im.tcp.receiver;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.yangxin.im.codec.proto.MessagePack;
import org.yangxin.im.common.ClientType;
import org.yangxin.im.common.constant.Constants;
import org.yangxin.im.common.enums.DeviceMultiLoginEnum;
import org.yangxin.im.common.enums.command.SystemCommand;
import org.yangxin.im.common.model.UserClientDto;
import org.yangxin.im.tcp.redis.RedisManager;
import org.yangxin.im.tcp.util.SessionSocketHolder;

import java.util.List;

/**
 * 多端同步：
 * 1 单端登录：一端在线，踢掉除了本 clientType + imel 的设备
 * 2 双端登录：允许 pc/mobile 其中一端登录 + web 端，踢掉除了本 clientType + imel 以外的 web 端设备
 * 3 三端登录：允许手机 + pc + web ，踢掉同端的其他 imei ，除了 web
 * 4 不做任何处理
 */
@Slf4j
public class UserLoginMessageListener {
    private final Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    private static boolean isIsSameClient(Integer clientType, UserClientDto dto) {
        boolean isSameClient = (clientType == ClientType.IOS.getCode() ||
                clientType == ClientType.ANDROID.getCode()) &&
                (dto.getClientType() == ClientType.IOS.getCode() ||
                        dto.getClientType() == ClientType.ANDROID.getCode());

        if ((clientType == ClientType.MAC.getCode() ||
                clientType == ClientType.WINDOWS.getCode()) &&
                (dto.getClientType() == ClientType.MAC.getCode() ||
                        dto.getClientType() == ClientType.WINDOWS.getCode())) {
            isSameClient = true;
        }
        return isSameClient;
    }

    public void listenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, (charSequence, msg) -> {
            log.info("收到用户上线通知：{}", msg);
            UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);
            List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());
            for (NioSocketChannel nioSocketChannel : nioSocketChannels) {
                Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                if (loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()) {
                    kickOffClient(dto, nioSocketChannel, clientType, imei);
                } else if (loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()) {
                    if (dto.getClientType() == ClientType.WEB.getCode()) {
                        continue;
                    }
                    if (clientType == ClientType.WEB.getCode()) {
                        continue;
                    }
                    kickOffClient(dto, nioSocketChannel, clientType, imei);
                } else if (loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()) {
                    if (dto.getClientType() == ClientType.WEB.getCode()) {
                        continue;
                    }
                    boolean isSameClient = isIsSameClient(clientType, dto);
                    if (isSameClient && !(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                        // 告诉客户端，其他端登录
                        doKickOffClient(nioSocketChannel);
                    }
                }
            }
        });
    }

    private void doKickOffClient(NioSocketChannel nioSocketChannel) {
        MessagePack<Object> pack = new MessagePack<>();
        pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
        pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
        pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
        nioSocketChannel.writeAndFlush(pack);
    }

    private void kickOffClient(UserClientDto dto, NioSocketChannel nioSocketChannel, Integer clientType, String imei) {
        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
            // 踢掉客户端
            // 告诉客户端，其他端登录
            doKickOffClient(nioSocketChannel);
        }
    }
}
