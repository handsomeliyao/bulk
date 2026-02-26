package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.AdminActionRequest;
import com.liyao.bulk.dto.AdminCreateRequest;
import com.liyao.bulk.dto.AdminDetailResponse;
import com.liyao.bulk.dto.AdminExportRow;
import com.liyao.bulk.dto.AdminModifyRequest;
import com.liyao.bulk.dto.AdminPermissionResponse;
import com.liyao.bulk.dto.AdminSummary;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/department-admins")
public class DepartmentAdminController {

    private final AdminService adminService;

    public DepartmentAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "查询部门管理员", description = "按条件查询部门管理员列表")
    public ApiResponse<List<AdminSummary>> queryAdmins(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "用户编码") @RequestParam(required = false) String operCode,
            @Parameter(description = "用户姓名") @RequestParam(required = false) String operName,
            @Parameter(description = "用户状态") @RequestParam(required = false) String operStatus) {
        return ApiResponse.success(adminService.queryAdmins(deptId, operCode, operName, operStatus));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询管理员详情", description = "根据管理员ID获取详情")
    public ApiResponse<AdminDetailResponse> getAdminDetail(@Parameter(description = "管理员ID") @PathVariable Long id,
                                                           @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(adminService.getAdminDetail(id, deptId));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "刷新管理员权限", description = "刷新并返回管理员权限范围")
    public ApiResponse<AdminPermissionResponse> refreshPermissions(@Parameter(description = "管理员ID") @PathVariable Long id,
                                                                   @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(adminService.refreshPermissions(id, deptId));
    }

    @GetMapping("/export")
    @Operation(summary = "导出部门管理员", description = "导出部门管理员查询结果")
    public ResponseEntity<StreamingResponseBody> exportAdmins(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "用户编码") @RequestParam(required = false) String operCode,
            @Parameter(description = "用户姓名") @RequestParam(required = false) String operName,
            @Parameter(description = "用户状态") @RequestParam(required = false) String operStatus) {
        List<AdminExportRow> rows = adminService.buildAdminExport(deptId, operCode, operName, operStatus);
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
    public ApiResponse<Void> createAdmin(@RequestBody AdminCreateRequest request) {
        adminService.createAdminApply(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/applications")
    @Operation(summary = "修改管理员申请", description = "修改部门管理员申请信息")
    public ApiResponse<Void> modifyAdmin(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminModifyRequest request) {
        adminService.modifyAdminApply(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/freeze")
    @Operation(summary = "冻结管理员申请", description = "提交冻结部门管理员申请")
    public ApiResponse<Void> freezeAdmin(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminActionRequest request) {
        adminService.freezeAdmin(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/unfreeze")
    @Operation(summary = "解冻管理员申请", description = "提交解冻部门管理员申请")
    public ApiResponse<Void> unfreezeAdmin(@Parameter(description = "申请ID") @PathVariable Long id,
                                           @RequestBody AdminActionRequest request) {
        adminService.unfreezeAdmin(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/reset-password")
    @Operation(summary = "重置密码申请", description = "提交管理员重置密码申请")
    public ApiResponse<Void> resetPassword(@Parameter(description = "申请ID") @PathVariable Long id,
                                           @RequestBody AdminActionRequest request) {
        adminService.resetPassword(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/cancel")
    @Operation(summary = "注销管理员申请", description = "提交注销部门管理员申请")
    public ApiResponse<Void> cancelAdmin(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminActionRequest request) {
        adminService.cancelAdmin(id, request);
        return ApiResponse.success();
    }
}
