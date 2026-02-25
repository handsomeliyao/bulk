package com.liyao.bulk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liyao.bulk.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginUserCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final String tokenHeader;
    private final String fallbackTokenHeader;
    private final String cacheKeyPrefix;
    private final long expireSeconds;
    private final Map<String, LocalCacheEntry> localCache = new ConcurrentHashMap<>();

    public LoginUserCacheService(StringRedisTemplate stringRedisTemplate,
                                 ObjectMapper objectMapper,
                                 @Value("${bulk.login-user.token-header:Authorization}") String tokenHeader,
                                 @Value("${bulk.login-user.fallback-token-header:X-Token}") String fallbackTokenHeader,
                                 @Value("${bulk.login-user.cache-key-prefix:login:token:}") String cacheKeyPrefix,
                                 @Value("${bulk.login-user.expire-seconds:7200}") long expireSeconds) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.tokenHeader = tokenHeader;
        this.fallbackTokenHeader = fallbackTokenHeader;
        this.cacheKeyPrefix = cacheKeyPrefix;
        this.expireSeconds = expireSeconds;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void cacheLoginUser(String token, CurrentLoginUser user) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Login token is required");
        }
        if (user == null || user.getOperCode() == null || user.getOperName() == null || user.getOperName().isBlank()
                || user.getLoginOperCode() == null || user.getLoginOperCode().isBlank()) {
            throw new BusinessException("Login user is invalid");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getOperCode());
        payload.put("operCode", user.getLoginOperCode());
        payload.put("operName", user.getOperName());
        payload.put("applicantDeptId", user.getApplicantDeptId());
        payload.put("applicantDeptName", user.getApplicantDeptName());
        try {
            String raw = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.opsForValue().set(cacheKeyPrefix + token, raw, Duration.ofSeconds(expireSeconds));
            localCache.put(token, new LocalCacheEntry(user, expireSeconds));
        } catch (Exception ex) {
            throw new BusinessException("Failed to cache login user");
        }
    }

    public CurrentLoginUser getRequiredCurrentUser() {
        HttpServletRequest request = currentRequest();
        String token = resolveToken(request);
        String raw = stringRedisTemplate.opsForValue().get(cacheKeyPrefix + token);
        if (raw == null || raw.isBlank()) {
            CurrentLoginUser cached = getLocalUser(token);
            if (cached == null) {
                throw new BusinessException("Login info expired, please login again");
            }
            return cached;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            Long userId = resolveLong(root, "userId", "id", "user_id");
            if (userId == null) {
                userId = resolveLong(root, "operCode");
            }
            String userName = resolveText(root, "operName", "userName", "name");
            Long deptId = resolveLong(root, "applicantDeptId", "deptId", "dept_id");
            String deptName = resolveText(root, "applicantDeptName", "deptName", "dept_name");
            String loginOperCode = resolveText(root, "operCode", "loginOperCode", "userCode", "user_code");
            if (userId == null || userName == null || userName.isBlank()
                    || loginOperCode == null || loginOperCode.isBlank()) {
                throw new BusinessException("Cached login user is incomplete");
            }
            return new CurrentLoginUser(userId, userName, deptId, deptName, loginOperCode);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Cached login user format is invalid");
        }
    }

    public void removeCurrentLoginToken() {
        HttpServletRequest request = currentRequest();
        String token = resolveToken(request);
        stringRedisTemplate.delete(cacheKeyPrefix + token);
        localCache.remove(token);
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new BusinessException("Cannot resolve current request");
        }
        return servletRequestAttributes.getRequest();
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        if ((token == null || token.isBlank()) && fallbackTokenHeader != null && !fallbackTokenHeader.isBlank()) {
            token = request.getHeader(fallbackTokenHeader);
        }
        if (token == null || token.isBlank()) {
            token = request.getHeader("token");
        }
        if (token == null || token.isBlank()) {
            token = request.getHeader("accessToken");
        }
        if (token == null || token.isBlank()) {
            throw new BusinessException("Missing login token");
        }
        token = stripAuthPrefix(token);
        if (token.isBlank()) {
            throw new BusinessException("Invalid login token");
        }
        return token;
    }

    private CurrentLoginUser getLocalUser(String token) {
        LocalCacheEntry entry = localCache.get(token);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            localCache.remove(token);
            return null;
        }
        return entry.user();
    }

    private record LocalCacheEntry(CurrentLoginUser user, Instant expiresAt) {
        LocalCacheEntry(CurrentLoginUser user, long expireSeconds) {
            this(user, Instant.now().plusSeconds(expireSeconds));
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private String stripAuthPrefix(String token) {
        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("bearer")) {
            return trimmed.substring(6).trim();
        }
        if (lower.startsWith("token")) {
            return trimmed.substring(5).trim();
        }
        return trimmed;
    }

    private Long resolveLong(JsonNode root, String... names) {
        for (JsonNode node : candidateNodes(root)) {
            if (node == null || node.isNull()) {
                continue;
            }
            for (String name : names) {
                JsonNode value = node.get(name);
                if (value == null || value.isNull()) {
                    continue;
                }
                if (value.isNumber()) {
                    return value.longValue();
                }
                if (value.isTextual() && !value.asText().isBlank()) {
                    try {
                        return Long.parseLong(value.asText());
                    } catch (NumberFormatException ignored) {
                        // Ignore non-numeric candidates and continue trying other keys.
                    }
                }
            }
        }
        return null;
    }

    private String resolveText(JsonNode root, String... names) {
        for (JsonNode node : candidateNodes(root)) {
            if (node == null || node.isNull()) {
                continue;
            }
            for (String name : names) {
                JsonNode value = node.get(name);
                if (value == null || value.isNull()) {
                    continue;
                }
                String text = value.asText();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private JsonNode[] candidateNodes(JsonNode root) {
        JsonNode user = root.get("user");
        JsonNode userInfo = root.get("userInfo");
        JsonNode data = root.get("data");
        return new JsonNode[]{root, user, userInfo, data};
    }
}
