package com.example.demo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP-аспект для логирования всех запросов к REST API:
 * пользователь, метод, путь, длительность, результат.
 */
@Aspect
@Component
public class RequestLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("app.audit");

    @Around("execution(* com.example.demo.controller..*(..))")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String user = getCurrentUser();
        String method = "";
        String path = "";

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                if (request != null) {
                    method = request.getMethod();
                    path = request.getRequestURI();
                    if (request.getQueryString() != null) {
                        path = path + "?" + request.getQueryString();
                    }
                }
            }
        } catch (Exception e) {
            // ignore if not in request context
        }

        String targetMethod = joinPoint.getSignature().getDeclaringType().getSimpleName() + "#" + joinPoint.getSignature().getName();
        log.info("[USER_ACTION] user={} | {} {} | handler={} | start", user, method, path, targetMethod);

        Object result;
        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[USER_ACTION] user={} | {} {} | handler={} | success | {} ms", user, method, path, targetMethod, duration);
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[USER_ACTION] user={} | {} {} | handler={} | error: {} | {} ms", user, method, path, targetMethod, t.getMessage(), duration);
            throw t;
        }
    }

    private static String getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null
                    && !"anonymousUser".equals(auth.getPrincipal().toString())) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "anonymous";
    }
}
