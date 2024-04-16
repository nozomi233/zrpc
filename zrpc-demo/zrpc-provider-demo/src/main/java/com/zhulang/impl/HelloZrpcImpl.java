package com.zhulang.impl;

import com.zhulang.HelloZrpc;

/**
 * @Author Nozomi
 * @Date 2024/4/16 20:40
 */

public class HelloZrpcImpl implements HelloZrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
