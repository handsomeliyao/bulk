package com.liyao.bulk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class PermissionMenuTreeItem {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "父菜单ID")
    private Long pid;

    @Schema(description = "菜单编码")
    private String menuCode;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单排序")
    private Integer menuOrder;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "菜单地址")
    private String url;

    @Schema(description = "按钮列表")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PermissionButtonItem> buttons;

    @Schema(description = "子菜单列表")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PermissionMenuTreeItem> children;
}
