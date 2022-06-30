package com.liang.im.netty.client;

import com.liang.im.proto.MessageProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端消息处理器
 * @author liang
 * @version 1.0
 * @date 2022/6/29 17:11
 */
@Slf4j
public class NettyClientMessageHandler extends SimpleChannelInboundHandler<MessageProto.Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProto.Message message) throws Exception {
        super.channelRead(channelHandlerContext, message);
        log.info("客户端收到消息:[{}]", message);
    }
}
