package com.pet.saas.filter;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String satoken = getSatoken(request);

        try {
            filterChain.doFilter(request, response);

            long costTime = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (satoken != null) {
                log.info("{} {} | status: {} | time: {}ms | satoken: {}", method, requestUri, status, costTime, satoken);
            } else {
                log.info("{} {} | status: {} | time: {}ms", method, requestUri, status, costTime);
            }

        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("{} {} | error: {} | time: {}ms", method, requestUri, e.getMessage(), costTime);
            throw e;
        }
    }

    private String getSatoken(HttpServletRequest request) {
        String header = request.getHeader("satoken");
        if (StrUtil.isNotBlank(header)) {
            return header;
        }
        String param = request.getParameter("satoken");
        if (StrUtil.isNotBlank(param)) {
            return param;
        }
        return null;
    }
}
