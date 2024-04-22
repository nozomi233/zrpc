package com.zhulang;

import com.zhulang.annotation.TryTimes;

/**
 * @Author Nozomi
 * @Date 2024/4/16 20:36
 */

public interface HelloZrpc {

    /**
     * 通用接口，server和client都需要依赖
     * @param msg 发送的具体的消息
     * @return 返回的结果
     */
    @TryTimes(tryTimes = 3,intervalTime = 3000)
    String sayHi(String msg);

}
