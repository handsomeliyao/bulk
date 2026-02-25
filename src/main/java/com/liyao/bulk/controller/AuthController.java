package com.liyao.bulk.controller;

import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.LoginRequest;
import com.liyao.bulk.dto.LoginResponse;
import com.liyao.bulk.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录并缓存登录用户信息到Redis")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "根据请求头token清理Redis登录缓存")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }
}