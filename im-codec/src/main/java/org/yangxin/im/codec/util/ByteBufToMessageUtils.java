package org.yangxin.im.codec.util;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.yangxin.im.codec.proto.Message;
import org.yangxin.im.codec.proto.MessageHeader;

/**
 * 私有协议规则，
 * 4位表示Command表示消息的开始，
 * 4位表示version
 * 4位表示clientType
 * 4位表示messageType
 * 4位表示appId
 * 4位表示imei长度
 * imei
 * 4位表示数据长度
 * data
 * 后续将解码方式加到数据头根据不同的解码方式解码，如pb，json，现在用json字符串
 */
@SuppressWarnings("DuplicatedCode")
public class ByteBufToMessageUtils {

    public static Message transition(ByteBuf in) {

        /* 获取command*/
        int command = in.readInt();

        /* 获取version*/
        int version = in.readInt();

        /* 获取clientType*/
        int clientType = in.readInt();

        /* 获取clientType*/
        int messageType = in.readInt();

        /* 获取appId*/
        int appId = in.readInt();

        /* 获取imeiLength*/
        int imeiLength = in.readInt();

        /* 获取bodyLen*/
        int bodyLen = in.readInt();

        if (in.readableBytes() < bodyLen + imeiLength) {
            in.resetReaderIndex();
            return null;
        }

        byte[] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        byte[] bodyData = new byte[bodyLen];
        in.readBytes(bodyData);


        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setClientType(clientType);
        messageHeader.setCommand(command);
        messageHeader.setLength(bodyLen);
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
        return message;
    }

}
