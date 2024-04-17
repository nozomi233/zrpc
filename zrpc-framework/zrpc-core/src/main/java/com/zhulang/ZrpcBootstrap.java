package com.zhulang;

import com.zhulang.discovery.Registry;
import com.zhulang.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Nozomi
 * @Date 2024/4/16 21:10
 */
@Slf4j
public class ZrpcBootstrap {
    // ZrpcBootstrap是个单例，我们希望每个应用程序只有一个实例

    private static final ZrpcBootstrap zrpcBootStrap = new ZrpcBootstrap();

    // 定义相关的一些基础配置

    private String appName = "default";

    private RegistryConfig registryConfig;

    private ProtocolConfig protocolConfig;

    private int port = 8088;

    // 注册中心
    private Registry registry;

    // 维护已经发布且暴露的服务列表 key-> interface的全限定名  value -> ServiceConfig
    private static final Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);


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
        this.appName = appName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @param registryConfig 注册中心
     * @return this当前实例
     */
    public ZrpcBootstrap registry(RegistryConfig registryConfig) {
        // 这里维护一个zookeeper实例，但是，如果这样写就会将zookeeper和当前工程耦合
        // 我们其实是更希望以后可以扩展更多种不同的实现
        // 尝试使用registryConfig获取一个注册中心，有点工厂方法设计模式的意思了
        this.registry = registryConfig.getRegistry();
        return this;
    }


    /**
     * ---------------------------服务提供方的相关api---------------------------------
     */

    public ZrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列号", protocolConfig.toString());
        }
        return this;
    }


    /**
     * 发布服务，将接口->实现，注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return this当前实例
     */
    public ZrpcBootstrap publish(ServiceConfig<?> service) {
        // 我们抽象了注册中心的概念，使用注册中心的一个实现完成注册;
        // 有人会想，此时此刻难到不是强耦合的么？
        registry.register(service);

        // 1、当服务调用方，通过接口、方法名、具体的方法参数列表发起调用，提供方怎么知道使用哪一个实现
        // (1) new 一个  （2）spring beanFactory.getBean(Class)  (3) 自己维护映射关系
        SERVERS_LIST.put(service.getInterface().getName(), service);

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

        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * ---------------------------服务调用方的相关api---------------------------------
     */

    public ZrpcBootstrap reference(ReferenceConfig<?> reference) {


        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        // 1、reference需要一个注册中心
        reference.setRegistry(registry);
        return this;
    }



}
