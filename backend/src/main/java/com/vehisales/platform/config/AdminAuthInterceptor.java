package com.vehisales.platform.config;

import com.vehisales.platform.exception.UnauthorizedException;
import com.vehisales.platform.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminAuthService adminAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!adminAuthService.isAuthorized(request.getHeader(HttpHeaders.AUTHORIZATION))) {
            throw new UnauthorizedException("Sign in required");
        }
        return true;
    }
}
