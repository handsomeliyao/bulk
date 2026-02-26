package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.PositionApplyExportRow;
import com.liyao.bulk.dto.PositionApplyQueryRequest;
import com.liyao.bulk.dto.PositionApplyReviewRequest;
import com.liyao.bulk.dto.PositionApplySummary;
import com.liyao.bulk.dto.PositionDetailResponse;
import com.liyao.bulk.service.ApplyCompareService;
import com.liyao.bulk.service.PositionService;
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
@RequestMapping("/api/position-applications")
public class PositionApplyController {

    private final PositionService positionService;
    private final ApplyCompareService applyCompareService;

    public PositionApplyController(PositionService positionService,
                                   ApplyCompareService applyCompareService) {
        this.positionService = positionService;
        this.applyCompareService = applyCompareService;
    }

    @GetMapping
    @Operation(summary = "查询岗位申请", description = "按条件查询岗位申请列表")
    public ApiResponse<List<PositionApplySummary>> queryApplies(PositionApplyQueryRequest request) {
        return ApiResponse.success(positionService.queryPositionApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出岗位申请", description = "导出岗位申请列表为 Excel")
    public ResponseEntity<StreamingResponseBody> exportApplies(PositionApplyQueryRequest request) {
        List<PositionApplyExportRow> rows = positionService.buildApplyExport(request);
        String fileName = URLEncoder.encode("岗位申请导出.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, PositionApplyExportRow.class)
                .sheet("岗位申请导出")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销岗位申请", description = "撤销指定岗位申请")
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @Parameter(description = "操作人ID") @RequestParam Long operatorId) {
        positionService.revokeApply(id, operatorId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "复核岗位申请", description = "复核指定岗位申请")
    public ApiResponse<Void> reviewApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody PositionApplyReviewRequest request) {
        positionService.reviewApply(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/compare")
    @Operation(summary = "比对岗位申请与正式数据", description = "返回未修改字段的正式值，已修改字段返回空")
    public ApiResponse<PositionDetailResponse> compareApply(@Parameter(description = "申请ID") @PathVariable Long id) {
        return ApiResponse.success(applyCompareService.comparePosition(id));
    }
}
