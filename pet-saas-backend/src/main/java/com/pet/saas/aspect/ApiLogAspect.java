package com.pet.saas.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.saas.common.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口日志切面
 * Debug 级别打印请求参数、响应结果
 */
@Slf4j
@Aspect
@Component
public class ApiLogAspect {

    private final ObjectMapper objectMapper;

    public ApiLogAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        log.debug("ApiLogAspect 初始化完成");
    }

    /**
     * 切入点：所有 controller 包下的所有 public 方法
     */
    @Pointcut("within(com.pet.saas.controller..*)")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：打印请求和响应日志（Debug 级别）
     */
    @Around("controllerPointcut() && execution(public * *(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String requestPath = request != null ? request.getRequestURI() : "unknown";

        if (log.isDebugEnabled()) {
            Map<String, Object> params = getParams(joinPoint, signature);
            log.debug("[{}] {}.{} | params: {}", requestPath, className, methodName, toJson(params));
        }

        Object result;
        try {
            result = joinPoint.proceed();

            if (log.isDebugEnabled()) {
                long costTime = System.currentTimeMillis() - startTime;
                log.debug("[{}] {}.{} | result: {} | time: {}ms", requestPath, className, methodName, formatResult(result), costTime);
            }

            return result;

        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[{}] {}.{} | error: {} | time: {}ms", requestPath, className, methodName, e.getMessage(), costTime, e);
            throw e;
        }
    }

    private Map<String, Object> getParams(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        Map<String, Object> params = new HashMap<>();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] != null && isSkipParam(args[i])) {
                    continue;
                }
                params.put(paramNames[i], args[i]);
            }
        }
        return params;
    }

    private boolean isSkipParam(Object arg) {
        String className = arg.getClass().getName();
        return className.startsWith("jakarta.servlet.")
                || className.startsWith("javax.servlet.")
                || arg instanceof org.springframework.web.multipart.MultipartFile;
    }

    private String formatResult(Object result) {
        if (result instanceof R<?> r) {
            try {
                Map<String, Object> map = new HashMap<>();
                map.put("code", r.getCode());
                map.put("message", r.getMessage());
                map.put("data", r.getData());
                return objectMapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                return "code=" + r.getCode() + ", message=" + r.getMessage();
            }
        }
        return toJson(result);
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
