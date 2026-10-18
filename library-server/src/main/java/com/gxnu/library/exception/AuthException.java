package com.gxnu.library.exception;

import lombok.Getter;

/**
 * 鉴权异常（401未登录 / 403无权限）
 */
@Getter
public class AuthException extends RuntimeException {

    private final Integer code;

    public AuthException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}
