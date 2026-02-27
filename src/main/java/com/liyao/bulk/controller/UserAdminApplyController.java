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
@RequestMapping("/api/user-admin-applications")
public class UserAdminApplyController {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REVIEWED = "REVIEWED";

    private final AdminService adminService;

    public UserAdminApplyController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "查询用户管理员申请")
    public ApiResponse<List<AdminApplySummary>> queryApplies(AdminApplyQueryRequest request) {
        return ApiResponse.success(adminService.queryAdminApplies(request));
    }

    @GetMapping("/pending")
    @Operation(summary = "查询用户管理员待复核申请")
    public ApiResponse<List<AdminApplySummary>> queryPendingApplies(AdminApplyQueryRequest request) {
        request.setStatusType(STATUS_PENDING);
        return ApiResponse.success(adminService.queryAdminApplies(request));
    }

    @GetMapping("/reviewed")
    @Operation(summary = "查询用户管理员已复核申请")
    public ApiResponse<List<AdminApplySummary>> queryReviewedApplies(AdminApplyQueryRequest request) {
        request.setStatusType(STATUS_REVIEWED);
        return ApiResponse.success(adminService.queryAdminApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出用户管理员申请")
    public ResponseEntity<StreamingResponseBody> exportApplies(AdminApplyQueryRequest request) {
        List<AdminApplyExportRow> rows = adminService.buildApplyExport(request);
        String fileName = URLEncoder.encode("用户管理员申请信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, AdminApplyExportRow.class)
                .sheet("用户管理员申请信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销用户管理员申请")
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @Parameter(description = "操作人ID") @RequestParam Long operatorId) {
        adminService.revokeApply(id, operatorId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "复核用户管理员申请")
    public ApiResponse<Void> reviewApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminApplyReviewRequest request) {
        adminService.reviewApply(id, request);
        return ApiResponse.success();
    }
}
