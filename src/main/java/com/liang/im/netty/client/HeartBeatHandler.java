package com.liang.im.netty.client;

import com.liang.im.netty.MessageBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 心跳处理器
 * @author liang
 * @version 1.0
 * @date 2022/6/29 16:16
 */
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    NettyClient nettyClient;

    public HeartBeatHandler(NettyClient nettyClient){
        this.nettyClient = nettyClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Channel channel = ctx.channel();
        log.info("channel激活:[{}]", channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // 断线重连
        Channel channel = ctx.channel();
        log.info("channel断开,5s后重新连接:[{}]", channel.remoteAddress());
        channel.eventLoop().schedule(()->{
            NettyClient.getInstance().connectToNetty();
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                Channel channel = ctx.channel();
                // 发送心跳
                channel.writeAndFlush(MessageBuilder.buildHeartBeatMessage("client1"));
            }
        }
    }
}
