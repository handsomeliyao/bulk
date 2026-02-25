package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class MenuTreeItem {
    @Schema(description = "Menu ID")
    private Long id;

    @Schema(description = "Parent menu ID")
    private Long pid;

    @Schema(description = "Menu code")
    private String menuCode;

    @Schema(description = "Menu name")
    private String menuName;

    @Schema(description = "Menu order")
    private Integer menuOrder;

    @Schema(description = "Menu icon")
    private String icon;

    @Schema(description = "Menu url")
    private String url;

    @Schema(description = "Children")
    private List<MenuTreeItem> children;
}
