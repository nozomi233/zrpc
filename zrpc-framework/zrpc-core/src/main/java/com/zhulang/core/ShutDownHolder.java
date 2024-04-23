package com.zhulang.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @Author Nozomi
 * @Date 2024/4/23 9:33
 */

public class ShutDownHolder {

    // 用来标记请求挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);

    // 用于请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
