package com.zhulang.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Author Nozomi
 * @Date 2024/4/14 18:28
 */

public class AppServer {

    private int port;

    public AppServer(int port) {
        this.port = port;
    }

    public void start() {
        // 1. 创建EventLoopGroup，老板只负责处理请求，之后将请求分发至worker,官方老板是工人的5倍
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        try {

            // 2. 需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 配置服务器
            serverBootstrap = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MyChannelHandler());
                        }
                    });

            // 4. 绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AppServer(8080).start();
    }
}
