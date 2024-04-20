package com.zhulang.exceptions;

/**
 * @Author Nozomi
 * @Date 2024/4/20 16:07
 */

public class SerializeException extends RuntimeException{

    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }
}
