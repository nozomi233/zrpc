package com.zhulang;

import com.zhulang.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * CommandLineRunner 容器启动后执行，或使用spring观察者设计模式，监听容器刷新完成的事件
 * @Author Nozomi
 * @Date 2024/4/23 10:35
 */
@Component
@Slf4j
public class ZrpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.info("zrpc 开始启动...");
        ZrpcBootstrap.getInstance()
                .application("first-zrpc-provider")
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .scan("com.zhulang.impl")
                // 启动服务
                .start();
    }
}