package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.AdminApplyExportRow;
import com.liyao.bulk.dto.AdminApplyQueryRequest;
import com.liyao.bulk.dto.AdminApplyReviewRequest;
import com.liyao.bulk.dto.AdminApplySummary;
import com.liyao.bulk.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/department-admin-applications")
public class DepartmentAdminApplyController {

    private final AdminService adminService;

    public DepartmentAdminApplyController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "查询管理员申请", description = "按条件查询部门管理员申请列表")
    /**
     * 菜单: 用户与权限管理-部门管理员申请管理-已复核查询/待复核查询
     * 功能: 查询部门管理员申请
     * 示例: /api/department-admin-applications
     */
    public ApiResponse<List<AdminApplySummary>> queryApplies(AdminApplyQueryRequest request) {
        return ApiResponse.success(adminService.queryAdminApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出管理员申请", description = "导出部门管理员申请查询结果")
    /**
     * 菜单: 用户与权限管理-部门管理员申请管理-下载
     * 功能: 导出部门管理员申请
     * 示例: /api/department-admin-applications/export
     */
    public ResponseEntity<StreamingResponseBody> exportApplies(AdminApplyQueryRequest request) {
        List<AdminApplyExportRow> rows = adminService.buildApplyExport(request);
        String fileName = URLEncoder.encode("部门管理员申请信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, AdminApplyExportRow.class)
                .sheet("部门管理员申请信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销管理员申请", description = "撤销指定的部门管理员申请")
    /**
     * 菜单: 用户与权限管理-部门管理员申请管理-撤销
     * 功能: 撤销部门管理员申请
     * 示例: /api/department-admin-applications/{id}/revoke
     */
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @Parameter(description = "操作人ID") @RequestParam Long operatorId) {
        adminService.revokeApply(id, operatorId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "审核管理员申请", description = "审核部门管理员申请并提交结果")
    /**
     * 菜单: 用户与权限管理-部门管理员申请管理-录入复核
     * 功能: 复核部门管理员申请
     * 示例: /api/department-admin-applications/{id}/review
     */
    public ApiResponse<Void> reviewApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminApplyReviewRequest request) {
        adminService.reviewApply(id, request);
        return ApiResponse.success();
    }
}
