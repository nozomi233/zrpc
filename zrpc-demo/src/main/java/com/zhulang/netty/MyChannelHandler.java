package com.zhulang.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * @Author Nozomi
 * @Date 2024/4/14 18:39
 */

public class MyChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 处理收到的数据，并反馈消息到客户端
        ByteBuf in = (ByteBuf) msg;
        System.out.println("服务端收到客户端消息：" + in.toString(CharsetUtil.UTF_8));

        // 可以通过ctx获取channel
        ctx.writeAndFlush(Unpooled.copiedBuffer("你好，我是服务端，已收到你发的消息", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 出现异常的时候执行的动作（打印并关闭通道）
        cause.printStackTrace();
        ctx.close();
    }
}
