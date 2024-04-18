package com.zhulang.channelhandler.handler;

import com.zhulang.ZrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 这是一个用来测试的类
 * @Author Nozomi
 * @Date 2024/4/18 16:28
 */

public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        // 服务提供方，给与的结果
        String result = msg.toString(Charset.defaultCharset());
        // 从全局的挂起的请求中寻找与之匹配的待处理的 cf
        CompletableFuture<Object> completableFuture = ZrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}

