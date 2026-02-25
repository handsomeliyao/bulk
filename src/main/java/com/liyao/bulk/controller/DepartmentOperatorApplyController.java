package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.OperatorApplyExportRow;
import com.liyao.bulk.dto.OperatorApplyQueryRequest;
import com.liyao.bulk.dto.OperatorApplyReviewRequest;
import com.liyao.bulk.dto.OperatorApplySummary;
import com.liyao.bulk.service.OperatorService;
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
@RequestMapping("/api/department-operator-applications")
public class DepartmentOperatorApplyController {

    private final OperatorService operatorService;

    public DepartmentOperatorApplyController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @GetMapping
    @Operation(summary = "查询操作员申请", description = "按条件查询部门操作员申请列表")
    /**
     * 菜单: 用户与权限管理-部门操作员申请管理-已复核查询/待复核查询
     * 功能: 查询部门操作员申请
     * 示例: /api/department-operator-applications
     */
    public ApiResponse<List<OperatorApplySummary>> queryApplies(OperatorApplyQueryRequest request) {
        return ApiResponse.success(operatorService.queryOperatorApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出操作员申请", description = "导出部门操作员申请查询结果")
    /**
     * 菜单: 用户与权限管理-部门操作员申请管理-下载
     * 功能: 导出部门操作员申请
     * 示例: /api/department-operator-applications/export
     */
    public ResponseEntity<StreamingResponseBody> exportApplies(OperatorApplyQueryRequest request) {
        List<OperatorApplyExportRow> rows = operatorService.buildApplyExport(request);
        String fileName = URLEncoder.encode("部门操作员申请信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, OperatorApplyExportRow.class)
                .sheet("部门操作员申请信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销操作员申请", description = "撤销指定的部门操作员申请")
    /**
     * 菜单: 用户与权限管理-部门操作员申请管理-撤销
     * 功能: 撤销部门操作员申请
     * 示例: /api/department-operator-applications/{id}/revoke
     */
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @Parameter(description = "操作人ID") @RequestParam Long operatorId) {
        operatorService.revokeApply(id, operatorId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "审核操作员申请", description = "审核部门操作员申请并提交结果")
    /**
     * 菜单: 用户与权限管理-部门操作员申请管理-录入复核
     * 功能: 复核部门操作员申请
     * 示例: /api/department-operator-applications/{id}/review
     */
    public ApiResponse<Void> reviewApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody OperatorApplyReviewRequest request) {
        operatorService.reviewApply(id, request);
        return ApiResponse.success();
    }
}
