package com.liyao.bulk.interceptor;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.common.ErrorCode;
import com.liyao.bulk.config.ApiAuthProperties;
import com.liyao.bulk.service.LoginUserCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiAuthInterceptor implements HandlerInterceptor {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String LOGOUT_PATH = "/api/auth/logout";

    private final LoginUserCacheService loginUserCacheService;
    private final ApiAuthProperties apiAuthProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public ApiAuthInterceptor(LoginUserCacheService loginUserCacheService,
                              ApiAuthProperties apiAuthProperties) {
        this.loginUserCacheService = loginUserCacheService;
        this.apiAuthProperties = apiAuthProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (isExemptPath(path)) {
            return true;
        }
        try {
            loginUserCacheService.getRequiredCurrentUser();
            return true;
        } catch (Exception ignored) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID, apiAuthProperties.getExpiredMessage());
        }
    }

    private boolean isExemptPath(String path) {
        if (antPathMatcher.match(LOGIN_PATH, path) || antPathMatcher.match(LOGOUT_PATH, path)) {
            return true;
        }
        List<String> whitelist = apiAuthProperties.getWhitelist();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        for (String pattern : whitelist) {
            if (pattern != null && !pattern.isBlank() && antPathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
