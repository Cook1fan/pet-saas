package com.pet.saas.common;

/**
 * 业务异常
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code = 500;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
