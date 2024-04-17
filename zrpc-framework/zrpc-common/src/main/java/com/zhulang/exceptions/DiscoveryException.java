package com.zhulang.exceptions;

/**
 * @Author Nozomi
 * @Date 2024/4/17 15:26
 */

public class DiscoveryException extends RuntimeException{

    public DiscoveryException() {
    }

    public DiscoveryException(String message) {
        super(message);
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}