package com.zhulang;

import com.zhulang.annotation.ZrpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Nozomi
 * @Date 2024/4/23 10:49
 */
@RestController
public class HelloController {

    // 需要注入一个代理对象
    @ZrpcReference
    private HelloZrpc helloZrpc;

    @GetMapping("hello")
    public String hello(){
        return helloZrpc.sayHi("nozomi的zrpc完成啦！");
    }

}