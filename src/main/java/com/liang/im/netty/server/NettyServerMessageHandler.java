package com.liang.im.netty.server;

import com.liang.im.proto.MessageProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器消息处理器
 * @author liang
 * @version 1.0
 * @date 2022/6/29 16:17
 */
@Slf4j
public class NettyServerMessageHandler extends SimpleChannelInboundHandler<MessageProto.Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProto.Message message) throws Exception {
        log.info("服务器收到消息:[{}]", message);
    }
}
