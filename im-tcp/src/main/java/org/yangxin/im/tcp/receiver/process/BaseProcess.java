package org.yangxin.im.tcp.receiver.process;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.yangxin.im.codec.proto.MessagePack;
import org.yangxin.im.tcp.util.SessionSocketHolder;

public abstract class BaseProcess {
    public abstract void processBefore();

    public void process(MessagePack<?> messagePack) {
        processBefore();
        NioSocketChannel channel = SessionSocketHolder.get(messagePack.getAppId(), messagePack.getToId(),
                messagePack.getClientType(), messagePack.getImei());
        if (channel != null) {
            channel.writeAndFlush(messagePack);
        }
        processAfter();
    }

    public abstract void processAfter();
}
