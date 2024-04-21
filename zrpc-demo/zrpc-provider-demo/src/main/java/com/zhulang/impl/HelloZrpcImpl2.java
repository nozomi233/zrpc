package com.zhulang.impl;

import com.zhulang.HelloZrpc;
import com.zhulang.HelloZrpc2;
import com.zhulang.annotation.ZrpcApi;

/**
 * @Author Nozomi
 * @Date 2024/4/16 20:40
 */
@ZrpcApi
public class HelloZrpcImpl2 implements HelloZrpc2 {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
