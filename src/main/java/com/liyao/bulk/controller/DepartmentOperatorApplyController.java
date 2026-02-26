package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.OperatorApplyExportRow;
import com.liyao.bulk.dto.OperatorApplyQueryRequest;
import com.liyao.bulk.dto.OperatorApplyReviewRequest;
import com.liyao.bulk.dto.OperatorApplySummary;
import com.liyao.bulk.dto.OperatorDetailResponse;
import com.liyao.bulk.service.ApplyCompareService;
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
    private final ApplyCompareService applyCompareService;

    public DepartmentOperatorApplyController(OperatorService operatorService,
                                             ApplyCompareService applyCompareService) {
        this.operatorService = operatorService;
        this.applyCompareService = applyCompareService;
    }

    @GetMapping
    @Operation(summary = "查询操作员申请", description = "按条件查询操作员申请列表")
    public ApiResponse<List<OperatorApplySummary>> queryApplies(OperatorApplyQueryRequest request) {
        return ApiResponse.success(operatorService.queryOperatorApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出操作员申请", description = "导出操作员申请列表为 Excel")
    public ResponseEntity<StreamingResponseBody> exportApplies(OperatorApplyQueryRequest request) {
        List<OperatorApplyExportRow> rows = operatorService.buildApplyExport(request);
        String fileName = URLEncoder.encode("操作员申请导出.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, OperatorApplyExportRow.class)
                .sheet("操作员申请导出")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销操作员申请", description = "撤销指定操作员申请")
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @Parameter(description = "操作人ID") @RequestParam Long operatorId) {
        operatorService.revokeApply(id, operatorId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "复核操作员申请", description = "复核指定操作员申请")
    public ApiResponse<Void> reviewApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody OperatorApplyReviewRequest request) {
        operatorService.reviewApply(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/compare")
    @Operation(summary = "比对操作员申请与正式数据", description = "返回未修改字段的正式值，已修改字段返回空")
    public ApiResponse<OperatorDetailResponse> compareApply(@Parameter(description = "申请ID") @PathVariable Long id) {
        return ApiResponse.success(applyCompareService.compareOperator(id));
    }
}
