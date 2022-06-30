package com.liang.im.netty.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liang.im.proto.MessageProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * netty客户端
 *
 * @author liang
 * @version 1.0
 * @date 2022/6/29 17:08
 */
@Slf4j
public class NettyClient {

    // 服务器主机地址
    @Getter
    @Setter
    private String host = "localhost";

    // 端口号
    @Getter
    @Setter
    private int port = 8888;

    // 通信通道
    private Channel channel;

    // workGroup
    private NioEventLoopGroup workGroup;

    // 启动器
    private Bootstrap bootstrap;

    // 客户端实例
    private static volatile NettyClient instance;

    // 用来维护所有发起的连接
    private final Map<String, InetSocketAddress> connectingMap = new HashMap<>();


    // 单例
    public static NettyClient getInstance() {
        if (instance == null) {
            synchronized (NettyClient.class) {
                if (instance == null) {
                    instance = new NettyClient();
                    instance.initBootstrap();
                    // 设置默认主机
                    instance.setHost(NettyConfig.DEFAULT_HOST);
                    // 设置默认端口号
                    instance.setPort(NettyConfig.DEFAULT_PORT);
                }
            }
        }
        return instance;
    }


    private NettyClient() {

    }

    /**
     * 初始化启动器
     */
    public void initBootstrap() {
        workGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS));
                        nioSocketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                        nioSocketChannel.pipeline().addLast(new ProtobufDecoder(MessageProto.Message.getDefaultInstance()));
                        nioSocketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                        nioSocketChannel.pipeline().addLast(new ProtobufEncoder());
                        nioSocketChannel.pipeline().addLast(new HeartBeatHandler(NettyClient.this));
                        nioSocketChannel.pipeline().addLast(new NettyClientMessageHandler());
                    }
                });
    }

    /**
     * 连接到netty
     */
    public void connectToNetty() {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        if (connectingMap.containsValue(socketAddress)){
            // 当前连接已存在
            return;
        }else{
            // 新连接,关闭之前的连接，重新连接
            if (channel != null){
                channel.close();
                channel = null;
            }
        }
        // 为本次连接生成一个id
        String connectId = UUID.randomUUID().toString();
        // 清空之前的连接，取消延时任务
        connectingMap.clear();
        // 添加本次连接
        connectingMap.put(connectId, socketAddress);
        // 开始连接
        connect(connectId, socketAddress);
    }

    /**
     * 连接
     * @param cId 连接id
     * @param socketAddress 主机地址
     */
    public void connect(String cId, InetSocketAddress socketAddress){

        // 已存在可用连接，则直接跳过
        if (channel != null && channel.isActive()){
            return;
        }

        try {
            ChannelFuture connect = bootstrap.connect(socketAddress).sync();
            // 连接成功
            channel = connect.channel();
            log.info("连接成功:[{}]", socketAddress);
        } catch (Exception e) {
            // 连接失败
            log.info("连接失败,5s后重试:[{}]", socketAddress);
            workGroup.schedule(() -> {
                // 5s后先判断是否连接已经变更，变更则不再重新连接
                if (connectingMap.containsKey(cId)) {
                    connect(cId, socketAddress);
                }
            }, 5, TimeUnit.SECONDS);
        }
    }

    /**
     * 发送消息
     * @param message
     */
    public void sendMsg(MessageProto.Message message) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (channel != null && channel.isActive()) {
            log.info("发送消息:[{}]", gson.toJson(message));
            channel.writeAndFlush(message);
        } else {
            log.error("channel不可用");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NettyClient nettyClient = NettyClient.getInstance();
        nettyClient.connectToNetty();
    }
}
