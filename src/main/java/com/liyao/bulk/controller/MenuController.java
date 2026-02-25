package com.liyao.bulk.controller;

import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.MenuTreeItem;
import com.liyao.bulk.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    @Operation(summary = "Query menu tree", description = "Query all menus as a tree")
    public ApiResponse<List<MenuTreeItem>> queryMenuTree() {
        return ApiResponse.success(menuService.queryMenuTree());
    }
}
