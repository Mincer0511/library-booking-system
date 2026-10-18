package com.gxnu.library.interceptor;

import com.gxnu.library.annotation.RequireRole;
import com.gxnu.library.common.result.ResultCode;
import com.gxnu.library.exception.AuthException;
import com.gxnu.library.utils.JwtUtils;
import com.gxnu.library.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * JWT 鉴权拦截器：解析 token 写入 UserContext，并校验 @RequireRole 角色
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 1. 解析 token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            throw new AuthException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg());
        }
        Claims claims = jwtUtils.parseToken(header.substring(TOKEN_PREFIX.length()));
        if (claims == null) {
            throw new AuthException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg());
        }

        // 2. 写入上下文
        UserContext.LoginUser loginUser = new UserContext.LoginUser(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("roleKey", String.class));
        UserContext.set(loginUser);

        // 3. 角色权限校验（方法注解优先于类注解）
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && !Arrays.asList(requireRole.value()).contains(loginUser.getRoleKey())) {
            throw new AuthException(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMsg());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
