package com.gxnu.library.aspect;

import com.gxnu.library.annotation.OperationLog;
import com.gxnu.library.entity.SysOperationLog;
import com.gxnu.library.mapper.SysOperationLogMapper;
import com.gxnu.library.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面：管理端写接口执行成功后自动留痕（异常失败不记录），日志写入失败不影响业务
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogMapper logMapper;

    @AfterReturning("@annotation(opLog)")
    public void record(OperationLog opLog) {
        try {
            SysOperationLog record = new SysOperationLog();
            UserContext.LoginUser loginUser = UserContext.get();
            record.setUserId(loginUser != null ? loginUser.getUserId() : null);
            record.setModule(opLog.module());
            record.setContent(opLog.content());
            record.setIp(currentIp());
            record.setCreateTime(LocalDateTime.now());
            logMapper.insert(record);
        } catch (Exception e) {
            log.error("[操作日志] 留痕失败: {}", e.getMessage());
        }
    }

    /** 取真实 IP：优先 X-Forwarded-For 首段 */
    private String currentIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            return request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
