package com.gxnu.library.aspect;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.gxnu.library.annotation.RepeatSubmit;
import com.gxnu.library.exception.BusinessException;
import com.gxnu.library.utils.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 防重复提交切面：
 * key = 用户ID + 方法签名 + 入参 JSON 的 MD5；interval 毫秒内重复请求直接拒绝。
 * 采用 ConcurrentHashMap 惰性清理（超过间隔的记录在写入前移除），无需额外定时任务。
 * 业务执行抛异常时移除记录，保证失败后可立即重试。
 */
@Aspect
@Component
public class RepeatSubmitAspect {

    private static final int MAX_CACHE_SIZE = 4096;

    private final ConcurrentHashMap<String, Long> submitCache = new ConcurrentHashMap<>();

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        long now = System.currentTimeMillis();
        int interval = repeatSubmit.interval();

        // 惰性清理：缓存过大时移除已过期的记录
        if (submitCache.size() > MAX_CACHE_SIZE) {
            submitCache.entrySet().removeIf(entry -> now - entry.getValue() > interval);
        }

        String key = buildKey(joinPoint);
        Long lastTime = submitCache.get(key);
        if (lastTime != null && now - lastTime < interval) {
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
        // putIfAbsent 保证并发下只有一个线程能写入成功时间戳
        Long prev = submitCache.putIfAbsent(key, now);
        if (prev != null && now - prev < interval) {
            throw new BusinessException("操作过于频繁，请稍后再试");
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 业务执行失败允许立即重试
            submitCache.remove(key);
            throw e;
        }
    }

    /** 用户ID + 方法完整签名 + 参数 JSON 的 MD5 作为防重 key */
    private String buildKey(ProceedingJoinPoint joinPoint) {
        Long userId = UserContext.currentUserId();
        String signature = joinPoint.getSignature().toLongString();
        String args;
        try {
            args = JSONUtil.toJsonStr(joinPoint.getArgs());
        } catch (Exception e) {
            args = Arrays.toString(joinPoint.getArgs());
        }
        return DigestUtil.md5Hex(userId + ":" + signature + ":" + args);
    }
}
