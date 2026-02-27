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
@RequestMapping("/api/user-operator-applications")
public class UserOperatorApplyController {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REVIEWED = "REVIEWED";

    private final OperatorService operatorService;

    public UserOperatorApplyController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @GetMapping
    @Operation(summary = "查询用户操作员申请")
    public ApiResponse<List<OperatorApplySummary>> queryApplies(OperatorApplyQueryRequest request) {
        return ApiResponse.success(operatorService.queryOperatorApplies(request));
    }

    @GetMapping("/pending")
    @Operation(summary = "查询用户操作员待复核申请")
    public ApiResponse<List<OperatorApplySummary>> queryPendingApplies(OperatorApplyQueryRequest request) {
        request.setStatusType(STATUS_PENDING);
        return ApiResponse.success(operatorService.queryOperatorApplies(request));
    }

    @GetMapping("/reviewed")
    @Operation(summary = "查询用户操作员已复核申请")
    public ApiResponse<List<OperatorApplySummary>> queryReviewedApplies(OperatorApplyQueryRequest request) {
        request.setStatusType(STATUS_REVIEWED);
        return ApiResponse.success(operatorService.queryOperatorApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出用户操作员申请")
    public ResponseEntity<StreamingResponseBody> exportApplies(OperatorApplyQueryRequest request) {
        List<OperatorApplyExportRow> rows = operatorService.buildApplyExport(request);
        String fileName = URLEncoder.encode("用户操作员申请信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, OperatorApplyExportRow.class)
                .sheet("用户操作员申请信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销用户操作员申请")
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @Parameter(description = "操作人ID") @RequestParam Long operatorId) {
        operatorService.revokeApply(id, operatorId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "复核用户操作员申请")
    public ApiResponse<Void> reviewApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody OperatorApplyReviewRequest request) {
        operatorService.reviewApply(id, request);
        return ApiResponse.success();
    }
}
