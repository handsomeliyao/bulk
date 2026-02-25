package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.dto.LoginRequest;
import com.liyao.bulk.dto.LoginResponse;
import com.liyao.bulk.mapper.DepartmentMapper;
import com.liyao.bulk.mapper.PlatformUserMapper;
import com.liyao.bulk.model.Department;
import com.liyao.bulk.model.PlatformUser;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_RESET = "RESET";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final PlatformUserMapper platformUserMapper;
    private final DepartmentMapper departmentMapper;
    private final LoginUserCacheService loginUserCacheService;

    public AuthService(PlatformUserMapper platformUserMapper,
                       DepartmentMapper departmentMapper,
                       LoginUserCacheService loginUserCacheService) {
        this.platformUserMapper = platformUserMapper;
        this.departmentMapper = departmentMapper;
        this.loginUserCacheService = loginUserCacheService;
    }

    public LoginResponse login(LoginRequest request) {
        validateRequest(request);
        String operCode = request.getOperCode().trim();
        LOGGER.info("Login attempt, operCode={}", operCode);
        PlatformUser user = platformUserMapper.selectByOperCode(operCode);
        if (user == null) {
            LOGGER.warn("Login failed, operCode not found: {}", operCode);
            throw new BusinessException("OperCode or password is incorrect");
        }
        String requestPassword = request.getPassword().trim();
        String storedPassword = user.getPassword() == null ? "" : user.getPassword().trim();
        if (storedPassword.isEmpty() || !storedPassword.equals(requestPassword)) {
            LOGGER.warn("Login failed, password mismatch: {}, storedLength={}", operCode, storedPassword.length());
            throw new BusinessException("OperCode or password is incorrect");
        }
        if (!STATUS_NORMAL.equals(user.getOperStatus()) && !STATUS_RESET.equals(user.getOperStatus())) {
            LOGGER.warn("Login failed, status not allowed: {}, status={}", operCode, user.getOperStatus());
            throw new BusinessException("User status does not allow login");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String deptName = resolveDeptName(user.getDeptId());
        CurrentLoginUser currentUser = new CurrentLoginUser(
                user.getId(),
                user.getOperName(),
                user.getDeptId(),
                deptName,
                user.getOperCode()
        );
        loginUserCacheService.cacheLoginUser(token, currentUser);
        LOGGER.info("Login success, operCode={}, tokenType=Bearer, expiresIn={}", operCode,
                loginUserCacheService.getExpireSeconds());
        return new LoginResponse(
                token,
                "Bearer",
                loginUserCacheService.getExpireSeconds(),
                user.getId(),
                user.getOperCode(),
                user.getOperName(),
                user.getUserType(),
                user.getOperStatus(),
                user.getDeptId(),
                deptName
        );
    }

    public void logout() {
        loginUserCacheService.removeCurrentLoginToken();
    }

    private void validateRequest(LoginRequest request) {
        if (request == null) {
            throw new BusinessException("Request is required");
        }
        if (request.getOperCode() == null || request.getOperCode().trim().isEmpty()) {
            throw new BusinessException("OperCode is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BusinessException("Password is required");
        }
    }

    private String resolveDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        Department department = departmentMapper.selectById(deptId);
        return department == null ? null : department.getDeptName();
    }
}
