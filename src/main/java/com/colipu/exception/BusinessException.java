package com.colipu.exception;

public class BusinessException extends BaseException {

    private Object data;

    public BusinessException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable throwable) {
        super(message,throwable);
    }

    @Override
    public int getCode() {
        return super.getCode();
    }

    public Object getData() {
        return data;
    }

}
