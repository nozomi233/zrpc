package com.zhulang.channelhandler.handler;

import com.zhulang.ServiceConfig;
import com.zhulang.ZrpcBootstrap;
import com.zhulang.enumeration.RequestType;
import com.zhulang.enumeration.RespCode;
import com.zhulang.protection.RateLimiter;
import com.zhulang.protection.TokenBuketRateLimiter;
import com.zhulang.transport.message.RequestPayload;
import com.zhulang.transport.message.ZrpcRequest;
import com.zhulang.transport.message.ZrpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @Author Nozomi
 * @Date 2024/4/18 23:48
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<ZrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ZrpcRequest zrpcRequest) throws Exception {
        // 1、先封装部分响应
        ZrpcResponse zrpcResponse = new ZrpcResponse();
        zrpcResponse.setRequestId(zrpcRequest.getRequestId());
        zrpcResponse.setCompressType(zrpcRequest.getCompressType());
        zrpcResponse.setSerializeType(zrpcRequest.getSerializeType());

        // 2、 获得通道
        Channel channel = channelHandlerContext.channel();

//        // 3、查看关闭的挡板是否打开，如果挡板已经打开，返回一个错误的响应
//        if( ShutDownHolder.BAFFLE.get() ){
//            zrpcResponse.setCode(RespCode.BECOLSING.getCode());
//            channel.writeAndFlush(zrpcResponse);
//            return;
//        }
//
//        // 4、计数器加一
//        ShutDownHolder.REQUEST_COUNTER.increment();

        // 4、完成限流相关的操作
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter =
                ZrpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();

        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBuketRateLimiter(10, 10);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();

        // 5、处理请求的逻辑
        // 限流
        if (!allowRequest) {
            // 需要封装响应并且返回了
            zrpcResponse.setCode(RespCode.RATE_LIMIT.getCode());
            // 处理心跳
        } else if (zrpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()) {
            // 需要封装响应并且返回
            zrpcResponse.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());

            // 正常调用
        } else {
            /** ---------------具体的调用过程--------------**/
            // （1）获取负载内容
            RequestPayload requestPayload = zrpcRequest.getRequestPayload();

            // （2）根据负载内容进行方法调用
            try {
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用。", zrpcRequest.getRequestId());
                }
                // （3）封装响应   我们是否需要考虑另外一个问题，响应码，响应类型
                zrpcResponse.setCode(RespCode.SUCCESS.getCode());
                zrpcResponse.setBody(result);
            } catch (Exception e){
                log.error("编号为【{}】的请求在调用过程中发生异常。",zrpcRequest.getRequestId(),e);
                zrpcResponse.setCode(RespCode.FAIL.getCode());
            }
        }

        // 6、写出响应
        channel.writeAndFlush(zrpcResponse);
//
//        // 7、计数器减一
//        ShutDownHolder.REQUEST_COUNTER.decrement();

    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        // 寻找到匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = ZrpcBootstrap.SERVERS_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        // 通过反射调用 1. 获取方法对象 2.执行invoke方法
        Object returnValue = null;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (InvocationTargetException | NoSuchMethodException  | IllegalAccessException e) {
            log.error("调用服务【{}】的方法【{}】时发生了异常。", interfaceName, methodName);
            throw new RuntimeException(e);
        }

        return returnValue;
    }


}
