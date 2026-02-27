package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class AdminPermissionResponse {
    @Schema(description = "授权权限")
    private List<ButtonAuthItem> assignAuth;

    @Schema(description = "操作权限")
    private List<ButtonAuthItem> operAuth;
}