package com.colipu.exception;

/**
 * 异常基类
 */
public class BaseException extends RuntimeException {
    private int code;

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public int getCode() {
        return code;
    }
}
