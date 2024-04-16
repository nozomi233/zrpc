package com.zhulang;

import com.zhulang.impl.HelloZrpcImpl;

/**
 * @Author Nozomi
 * @Date 2024/4/16 20:49
 */

public class Application {

    public static void main(String[] args) {
        // 服务提供方，需要注册服务，启动服务
        // 1、封装要发布的服务
        ServiceConfig<HelloZrpc> service = new ServiceConfig<>();
        service.setInterface(HelloZrpc.class);
        service.setRef(new HelloZrpcImpl());

        // 2、定义注册中心

        // 3、通过启动引导程序，启动服务提供方
        //   （1） 配置 -- 应用的名称 -- 注册中心 -- 序列化协议 -- 压缩方式
        //   （2） 发布服务
        ZrpcBootstrap.getInstance()
                .application("first-zrpc-provider")
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                // 发布服务
                .publish(service)
                .start();
    }
}
