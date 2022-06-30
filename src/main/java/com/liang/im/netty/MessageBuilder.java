package com.liang.im.netty;

import com.liang.im.proto.MessageProto;

/**
 * 消息构造工具
 * @author liang
 * @version 1.0
 * @date 2022/6/29 16:59
 */
public class MessageBuilder {

    /**
     * 心跳消息
     * @param clientId 客户端id
     * @return
     */
    public static MessageProto.Message buildHeartBeatMessage(String clientId) {
        return MessageProto.Message.newBuilder()
                .setMessageType(MessageProto.Message.MessageType.HEARTBEAT)
                .setHeartbeat(MessageProto.Message.Heartbeat.newBuilder()
                        .setClientId(clientId)
                        .build())
                .build();
    }

    /**
     * 认证消息
     * @param id 客户端id
     * @param address 客户端地址
     * @param name 客户端名称
     * @return
     */
    public static MessageProto.Message buildAuthenticationMessage(String id, String address, String name) {
        return MessageProto.Message.newBuilder()
                .setMessageType(MessageProto.Message.MessageType.AUTHENTICATION)
                .setAuthentication(MessageProto.Message.Authentication.newBuilder()
                        .setClientId(id)
                        .setClientAddress(address)
                        .setClientName(name)
                        .build())
                .build();
    }
}
