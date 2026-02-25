package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PermissionButtonItem {
    @Schema(description = "按钮ID")
    private Long id;

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "按钮编码")
    private String btnCode;

    @Schema(description = "按钮名称")
    private String btnName;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求路径")
    private String uri;
}