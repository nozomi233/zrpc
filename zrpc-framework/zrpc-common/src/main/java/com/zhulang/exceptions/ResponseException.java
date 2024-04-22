package com.zhulang.exceptions;

/**
 * @Author Nozomi
 * @Date 2024/4/22 22:59
 */
public class ResponseException extends RuntimeException {

    private byte code;
    private String msg;

    public ResponseException(byte code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
