package com.zhulang.exceptions;

/**
 * @Author Nozomi
 * @Date 2024/4/20 18:24
 */

public class CompressException extends RuntimeException{

    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
