package com.zhulang.channelhandler.handler;

import com.zhulang.ServiceConfig;
import com.zhulang.ZrpcBootstrap;
import com.zhulang.enumeration.RequestType;
import com.zhulang.enumeration.RespCode;
import com.zhulang.transport.message.RequestPayload;
import com.zhulang.transport.message.ZrpcRequest;
import com.zhulang.transport.message.ZrpcResponse;
import io.netty.channel.Channel;
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
        // 1. 获取负载内容
        RequestPayload requestPayload = zrpcRequest.getRequestPayload();

        // 2. 根据负载内容进行方法调用
        Object result = null;
        if (!(zrpcRequest.getRequestType() == RequestType.HEART_BEAT.getId())){
            result = callTargetMethod(requestPayload);

            if (log.isDebugEnabled()){
                log.debug("请求【{}】已经在服务端完成方法调用。", zrpcRequest.getRequestId());
            }
        }


        // 3. 封装响应
        ZrpcResponse zrpcResponse = new ZrpcResponse();
        zrpcResponse.setCode(RespCode.SUCCESS.getCode());
        zrpcResponse.setRequestId(zrpcRequest.getRequestId());
        zrpcResponse.setCompressType(zrpcRequest.getCompressType());
        zrpcResponse.setSerializeType(zrpcRequest.getSerializeType());
        zrpcResponse.setBody(result);

        // 4. 写出响应
        channelHandlerContext.channel().writeAndFlush(zrpcResponse);

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
