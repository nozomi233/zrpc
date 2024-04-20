package com.zhulang;

import com.zhulang.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Nozomi
 * @Date 2024/4/16 20:59
 */
@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) {
        // 想尽一切办法获取代理对象,使用ReferenceConfig进行封装
        // reference一定用生成代理的模板方法，get()
        ReferenceConfig<HelloZrpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloZrpc.class);

        // 代理做了些什么?
        // 1、连接注册中心
        // 2、拉取服务列表
        // 3、选择一个服务并建立连接
        // 4、发送请求，携带一些信息（接口名，参数列表，方法的名字），获得结果
        ZrpcBootstrap.getInstance()
                .application("first-zrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .reference(reference);

        HelloZrpc helloZrpc = reference.get();
        String sayHi = helloZrpc.sayHi("你好,nozomi");
        log.info("sayHi-->{}", sayHi);
    }
}
