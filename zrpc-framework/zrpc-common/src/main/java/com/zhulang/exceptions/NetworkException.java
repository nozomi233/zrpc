package com.zhulang.exceptions;

/**
 * @Author Nozomi
 * @Date 2024/4/17 14:29
 */

public class NetworkException extends RuntimeException{

    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
}
