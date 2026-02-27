package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.AdminActionRequest;
import com.liyao.bulk.dto.AdminCreateRequest;
import com.liyao.bulk.dto.AdminDetailResponse;
import com.liyao.bulk.dto.AdminExportRow;
import com.liyao.bulk.dto.AdminModifyRequest;
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
@RequestMapping("/api/user-admins")
public class UserAdminController {

    private final AdminService adminService;

    public UserAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "查询用户管理员")
    public ApiResponse<List<AdminSummary>> queryAdmins(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "用户编码") @RequestParam(required = false) String operCode,
            @Parameter(description = "用户名称") @RequestParam(required = false) String operName,
            @Parameter(description = "用户状态") @RequestParam(required = false) String operStatus) {
        return ApiResponse.success(adminService.queryAdmins(deptId, operCode, operName, operStatus));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户管理员详情")
    public ApiResponse<AdminDetailResponse> getAdminDetail(@Parameter(description = "用户ID") @PathVariable Long id,
                                                           @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(adminService.getAdminDetail(id, deptId));
    }

    @GetMapping("/export")
    @Operation(summary = "导出用户管理员")
    public ResponseEntity<StreamingResponseBody> exportAdmins(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "用户编码") @RequestParam(required = false) String operCode,
            @Parameter(description = "用户名称") @RequestParam(required = false) String operName,
            @Parameter(description = "用户状态") @RequestParam(required = false) String operStatus) {
        List<AdminExportRow> rows = adminService.buildAdminExport(deptId, operCode, operName, operStatus);
        String fileName = URLEncoder.encode("用户管理员信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, AdminExportRow.class)
                .sheet("用户管理员信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/applications")
    @Operation(summary = "新增用户管理员申请")
    public ApiResponse<Void> createAdmin(@RequestBody AdminCreateRequest request) {
        adminService.createAdminApply(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/applications")
    @Operation(summary = "修改用户管理员申请")
    public ApiResponse<Void> modifyAdmin(@Parameter(description = "用户ID") @PathVariable Long id,
                                         @RequestBody AdminModifyRequest request) {
        adminService.modifyAdminApply(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/cancel")
    @Operation(summary = "注销用户管理员申请")
    public ApiResponse<Void> cancelAdmin(@Parameter(description = "用户ID") @PathVariable Long id,
                                         @RequestBody(required = false) AdminActionRequest request) {
        adminService.cancelAdmin(id, request == null ? new AdminActionRequest() : request);
        return ApiResponse.success();
    }
}
