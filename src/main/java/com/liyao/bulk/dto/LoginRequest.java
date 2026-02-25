package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequest {
    @Schema(description = "用户ID")
    private String operCode;

    @Schema(description = "密码")
    private String password;
}
