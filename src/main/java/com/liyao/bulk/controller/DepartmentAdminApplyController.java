package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.AdminApplyExportRow;
import com.liyao.bulk.dto.AdminApplyQueryRequest;
import com.liyao.bulk.dto.AdminApplyReviewRequest;
import com.liyao.bulk.dto.AdminApplySummary;
import com.liyao.bulk.dto.AdminDetailResponse;
import com.liyao.bulk.service.AdminService;
import com.liyao.bulk.service.ApplyCompareService;
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
    private final ApplyCompareService applyCompareService;

    public DepartmentAdminApplyController(AdminService adminService,
                                          ApplyCompareService applyCompareService) {
        this.adminService = adminService;
        this.applyCompareService = applyCompareService;
    }

    @GetMapping
    @Operation(summary = "查询管理员申请", description = "按条件查询管理员申请列表")
    public ApiResponse<List<AdminApplySummary>> queryApplies(AdminApplyQueryRequest request) {
        return ApiResponse.success(adminService.queryAdminApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出管理员申请", description = "导出管理员申请列表为 Excel")
    public ResponseEntity<StreamingResponseBody> exportApplies(AdminApplyQueryRequest request) {
        List<AdminApplyExportRow> rows = adminService.buildApplyExport(request);
        String fileName = URLEncoder.encode("管理员申请导出.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, AdminApplyExportRow.class)
                .sheet("管理员申请导出")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销管理员申请", description = "撤销指定管理员申请")
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @Parameter(description = "操作人ID") @RequestParam Long operatorId) {
        adminService.revokeApply(id, operatorId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "复核管理员申请", description = "复核指定管理员申请")
    public ApiResponse<Void> reviewApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody AdminApplyReviewRequest request) {
        adminService.reviewApply(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/compare")
    @Operation(summary = "比对管理员申请与正式数据", description = "返回未修改字段的正式值，已修改字段返回空")
    public ApiResponse<AdminDetailResponse> compareApply(@Parameter(description = "申请ID") @PathVariable Long id) {
        return ApiResponse.success(applyCompareService.compareAdmin(id));
    }
}
