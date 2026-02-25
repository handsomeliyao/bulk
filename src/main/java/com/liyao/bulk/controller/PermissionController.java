package com.liyao.bulk.controller;

import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.PermissionMenuTreeItem;
import com.liyao.bulk.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/tree")
    @Operation(summary = "查询权限树", description = "查询所有菜单以及各菜单下所有按钮")
    public ApiResponse<List<PermissionMenuTreeItem>> queryPermissionTree() {
        return ApiResponse.success(permissionService.queryPermissionTree());
    }
}
