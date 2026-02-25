package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.*;
import com.liyao.bulk.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/department-admins")
public class DepartmentAdminController {

    private final AdminService adminService;

    public DepartmentAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "查询部门管理员", description = "按条件查询部门管理员列表")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-查询
     * 功能: 查询部门管理员
     * 示例: /api/department-admins
     */
    public ApiResponse<List<AdminSummary>> queryAdmins(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "用户名") @RequestParam(required = false) String operCode,
            @Parameter(description = "姓名") @RequestParam(required = false) String operName,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        return ApiResponse.success(adminService.queryAdmins(deptId, operCode, operName, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询管理员详情", description = "根据管理员ID获取详情")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-详情
     * 功能: 查看部门管理员详情
     * 示例: /api/department-admins/{id}
     */
    public ApiResponse<AdminDetailResponse> getAdminDetail(@Parameter(description = "管理员ID") @PathVariable Long id,
                                                           @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(adminService.getAdminDetail(id, deptId));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "刷新管理员权限", description = "刷新并返回管理员权限范围")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-详情
     * 功能: 刷新部门管理员权限
     * 示例: /api/department-admins/{id}/permissions
     */
    public ApiResponse<AdminPermissionResponse> refreshPermissions(@Parameter(description = "管理员ID") @PathVariable Long id,
                                                                   @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(adminService.refreshPermissions(id, deptId));
    }

    @GetMapping("/export")
    @Operation(summary = "导出部门管理员", description = "导出部门管理员查询结果")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-下载
     * 功能: 导出部门管理员查询结果
     * 示例: /api/department-admins/export
     */
    public ResponseEntity<StreamingResponseBody> exportAdmins(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "用户名") @RequestParam(required = false) String operCode,
            @Parameter(description = "姓名") @RequestParam(required = false) String operName,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        List<AdminExportRow> rows = adminService.buildAdminExport(deptId, operCode, operName, status);
        String fileName = URLEncoder.encode("部门管理员信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, AdminExportRow.class)
                .sheet("部门管理员信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/applications")
    @Operation(summary = "新增管理员申请", description = "提交新增部门管理员申请")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-新增
     * 功能: 新增部门管理员申请
     * 示例: /api/department-admins/applications
     */
    public ApiResponse<Void> createAdmin(@RequestBody AdminCreateRequest request) {
        adminService.createAdminApply(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/applications")
    @Operation(summary = "修改管理员申请", description = "修改部门管理员申请信息")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-修改
     * 功能: 修改部门管理员申请
     * 示例: /api/department-admins/{id}/applications
     */
    public ApiResponse<Void> modifyAdmin(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminModifyRequest request) {
        adminService.modifyAdminApply(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/freeze")
    @Operation(summary = "冻结管理员申请", description = "提交冻结部门管理员申请")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-冻结
     * 功能: 冻结部门管理员申请
     * 示例: /api/department-admins/{id}/applications/freeze
     */
    public ApiResponse<Void> freezeAdmin(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminActionRequest request) {
        adminService.freezeAdmin(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/unfreeze")
    @Operation(summary = "解冻管理员申请", description = "提交解冻部门管理员申请")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-解冻
     * 功能: 解冻部门管理员申请
     * 示例: /api/department-admins/{id}/applications/unfreeze
     */
    public ApiResponse<Void> unfreezeAdmin(@Parameter(description = "申请ID") @PathVariable Long id,
                                           @RequestBody AdminActionRequest request) {
        adminService.unfreezeAdmin(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/reset-password")
    @Operation(summary = "重置密码申请", description = "提交管理员重置密码申请")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-重置密码
     * 功能: 重置部门管理员密码申请
     * 示例: /api/department-admins/{id}/applications/reset-password
     */
    public ApiResponse<Void> resetPassword(@Parameter(description = "申请ID") @PathVariable Long id,
                                           @RequestBody AdminActionRequest request) {
        adminService.resetPassword(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/cancel")
    @Operation(summary = "注销管理员申请", description = "提交注销部门管理员申请")
    /**
     * 菜单: 用户与权限管理-部门管理员维护-注销
     * 功能: 注销部门管理员申请
     * 示例: /api/department-admins/{id}/applications/cancel
     */
    public ApiResponse<Void> cancelAdmin(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminActionRequest request) {
        adminService.cancelAdmin(id, request);
        return ApiResponse.success();
    }

    
}
