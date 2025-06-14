package org.yangxin.im.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.yangxin.im.codec.proto.MessagePack;

@SuppressWarnings("rawtypes")
public class MessageEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        if (msg instanceof MessagePack) {
            MessagePack msgBody = (MessagePack) msg;
            String s = JSONObject.toJSONString(msgBody.getData());
            byte[] bytes = s.getBytes();
            out.writeInt(msgBody.getCommand());
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }
}
