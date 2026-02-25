package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OperatorPermissionItem {
    @Schema(description = "系统编码")
    private String systemCode;
    @Schema(description = "系统名称")
    private String systemName;
    @Schema(description = "权限编码")
    private String scopeCode;
    @Schema(description = "权限名称")
    private String scopeName;
    @Schema(description = "是否选中")
    private boolean selected;
}
