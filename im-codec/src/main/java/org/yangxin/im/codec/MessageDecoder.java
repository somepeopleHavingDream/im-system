package org.yangxin.im.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.yangxin.im.codec.proto.Message;
import org.yangxin.im.codec.proto.MessageHeader;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 28) {
            return;
        }

        // 获取 command
        int command = in.readInt();
        // 获取 version
        int version = in.readInt();
        // 获取 clientType
        int clientType = in.readInt();
        // 获取 messageType
        int messageType = in.readInt();
        // 获取 appId
        int appId = in.readInt();
        // 获取 imeiLength
        int imeiLength = in.readInt();
        // 获取 bodyLength
        int bodyLength = in.readInt();

        if (in.readableBytes() < imeiLength + bodyLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        byte[] bodyData = new byte[bodyLength];
        in.readBytes(bodyData);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setClientType(clientType);
        messageHeader.setCommand(command);
        messageHeader.setLength(bodyLength);
        messageHeader.setVersion(version);
        messageHeader.setMessageType(messageType);
        messageHeader.setImei(imei);

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        if (messageType == 0x0) {
            String body = new String(bodyData);
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(parse);
        }

        in.markReaderIndex();
        out.add(message);
    }
}
