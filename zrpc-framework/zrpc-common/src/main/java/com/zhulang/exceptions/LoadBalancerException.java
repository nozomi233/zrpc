package com.zhulang.exceptions;

/**
 * @Author Nozomi
 * @Date 2024/4/20 19:36
 */

public class LoadBalancerException extends RuntimeException {
    public LoadBalancerException(String message) {
        super(message);
    }

    public LoadBalancerException() {
    }
}
