package com.gxnu.library.utils;

import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.exception.AuthException;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前登录用户上下文（ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前用户ID，未登录抛 401
     */
    public static Long currentUserId() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new AuthException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg());
        }
        return loginUser.getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 登录用户信息
     */
    @Data
    @AllArgsConstructor
    public static class LoginUser {
        private Long userId;
        private String username;
        private String roleKey;
    }
}
