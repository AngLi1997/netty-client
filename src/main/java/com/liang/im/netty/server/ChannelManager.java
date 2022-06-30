package com.liang.im.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * 通道管理
 * @author liang
 * @version 1.0
 * @date 2022/6/29 17:20
 */
@Slf4j
public class ChannelManager extends ChannelInboundHandlerAdapter {

    // 维护所有已连接的通道
    private final Map<SocketAddress,Channel> channelMap = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Channel channel = ctx.channel();
        channelMap.put(channel.remoteAddress(), channel);
        log.info("客户端连接...地址:[{}]", channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel channel = ctx.channel();
        channelMap.remove(channel.remoteAddress());
        log.info("客户端断开...地址:[{}]", channel.remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                SocketAddress socketAddress = channel.remoteAddress();
                if (channelMap.containsKey(socketAddress)) {
                    log.info("客户端心跳超时...地址:[{}]", socketAddress);
                    channelMap.remove(socketAddress);
                    channel.disconnect();
                }
            }
        }
    }
}
