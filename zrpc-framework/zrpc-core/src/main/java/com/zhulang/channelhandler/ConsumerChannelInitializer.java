package com.zhulang.channelhandler;

import com.zhulang.channelhandler.handler.MySimpleChannelInboundHandler;
import com.zhulang.channelhandler.handler.ZrpcRequestEncoder;
import com.zhulang.channelhandler.handler.ZrpcResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Author Nozomi
 * @Date 2024/4/18 16:27
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                // netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 消息编码器
                .addLast(new ZrpcRequestEncoder())
                // 入站的解码器
                .addLast(new ZrpcResponseDecoder())
                // 处理结果


                .addLast(new MySimpleChannelInboundHandler());

    }
}

