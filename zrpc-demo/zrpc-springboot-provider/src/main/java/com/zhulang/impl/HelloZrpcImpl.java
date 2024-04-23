package com.zhulang.impl;

import com.zhulang.HelloZrpc;
import com.zhulang.annotation.ZrpcApi;

/**
 * @Author Nozomi
 * @Date 2024/4/16 20:40
 */
@ZrpcApi(group = "primary")
public class HelloZrpcImpl implements HelloZrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
