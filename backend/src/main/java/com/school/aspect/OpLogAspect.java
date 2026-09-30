package com.school.aspect;

import com.school.common.OpLog;
import com.school.common.UserContext;
import com.school.entity.OperationLog;
import com.school.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面：只追加，不修改
 */
@Slf4j
@Aspect
@Component
public class OpLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            saveLog(opLog, null);
            return result;
        } catch (Throwable t) {
            saveLog(opLog, t.getMessage());
            throw t;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("op log cost {}ms", System.currentTimeMillis() - start);
            }
        }
    }

    private void saveLog(OpLog opLog, String errMsg) {
        try {
            UserContext ctx = UserContext.get();
            OperationLog l = new OperationLog();
            if (ctx != null) {
                l.setSchoolId(ctx.getSchoolId() == null ? 0L : ctx.getSchoolId());
                l.setOperatorId(ctx.getUserId());
                l.setOperatorRole(ctx.getRole());
            }
            l.setModule(opLog.module());
            l.setAction(opLog.action());
            l.setTargetType(opLog.targetType());
            HttpServletRequest req = currentRequest();
            if (req != null) {
                l.setIp(clientIp(req));
                l.setUserAgent(req.getHeader("User-Agent"));
            }
            operationLogMapper.insert(l);
        } catch (Exception e) {
            log.warn("记录操作日志失败: {}", e.getMessage());
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private String clientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip == null ? "" : ip.split(",")[0].trim();
    }
}
