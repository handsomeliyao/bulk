package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScopeItem {
    @Schema(description = "权限编码")
    private String scopeCode;
    @Schema(description = "权限名称")
    private String scopeName;
}
