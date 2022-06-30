package com.liang.im.netty.server;

import com.liang.im.proto.MessageProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author liang
 * @version 1.0
 * @date 2022/6/29 16:05
 */
@Slf4j
public class NettyServer {

    private static final int port = 8888;

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup handlerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, handlerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new IdleStateHandler(20, 0, 0, TimeUnit.SECONDS));
                        socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                        socketChannel.pipeline().addLast(new ProtobufDecoder(MessageProto.Message.getDefaultInstance()));
                        socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                        socketChannel.pipeline().addLast(new ProtobufEncoder());
                        socketChannel.pipeline().addLast(new ChannelManager());
                        socketChannel.pipeline().addLast(new NettyServerMessageHandler());
                    }
                });
        ChannelFuture sync = serverBootstrap.bind(port).sync();
        if (sync.isSuccess()){
            log.info("服务已启动...端口:[{}]", port);
        }
    }
}
