package org.yangxin.im.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.yangxin.im.codec.proto.Message;
import org.yangxin.im.codec.util.ByteBufToMessageUtils;

import java.util.List;

public class WebSocketMessageDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) {

        ByteBuf content = msg.content();
        if (content.readableBytes() < 28) {
            return;
        }
        Message message = ByteBufToMessageUtils.transition(content);
        if (message == null) {
            return;
        }
        out.add(message);
    }
}
