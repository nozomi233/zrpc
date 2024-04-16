package com.zhulang;

import java.util.List;

/**
 * @Author Nozomi
 * @Date 2024/4/16 21:10
 */

public class ZrpcBootstrap {
    // ZrpcBootstrap是个单例，我们希望每个应用程序只有一个实例

    private static ZrpcBootstrap zrpcBootStrap = new ZrpcBootstrap();

    private ZrpcBootstrap() {
        // 构造启动引导程序，时需要做一些什么初始化的事
    }

    public static ZrpcBootstrap getInstance() {
        return zrpcBootStrap;
    }

    /**
     * 用来定义当前应用的名字
     *
     * @param appName 应用的名字
     * @return this当前实例
     */
    public ZrpcBootstrap application(String appName) {
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @param registryConfig 注册中心
     * @return this当前实例
     */
    public ZrpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }


    /**
     * ---------------------------服务提供方的相关api---------------------------------
     */

    public ZrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        return null;
    }


    /**
     * 发布服务，将接口->实现，注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return this当前实例
     */
    public ZrpcBootstrap publish(ServiceConfig<?> service) {
        return this;
    }


    /**
     * 批量发布
     *
     * @param services 封装的需要发布的服务集合
     * @return this当前实例
     */
    public ZrpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }



    /**
     * 启动netty服务
     */
    public void start() {
    }


    /**
     * ---------------------------服务调用方的相关api---------------------------------
     */

    public ZrpcBootstrap reference(ReferenceConfig<?> reference) {


        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        // 1、reference需要一个注册中心
        return this;
    }



}
