package com.zhulang.protection;

/**
 * @Author Nozomi
 * @Date 2024/4/22 21:00
 */
public interface RateLimiter {

    /**
     * 是否允许新的请求进入
     * @return true 可以进入  false  拦截
     */
    boolean allowRequest();
}
