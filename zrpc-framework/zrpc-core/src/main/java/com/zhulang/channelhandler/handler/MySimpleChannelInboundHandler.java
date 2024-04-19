package com.zhulang.channelhandler.handler;

import com.zhulang.ZrpcBootstrap;
import com.zhulang.transport.message.ZrpcResponse;
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
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ZrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ZrpcResponse zrpcResponse) throws Exception {
        // 服务提供方，给予的结果
        Object returnValue = zrpcResponse.getBody();
        // 从全局的挂起的请求中寻找与之匹配的待处理的 cf
        CompletableFuture<Object> completableFuture = ZrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()){
            log.debug("已寻找到编号为【{}】的completableFuture，处理响应结果。", zrpcResponse.getRequestId());
        }
    }
}

