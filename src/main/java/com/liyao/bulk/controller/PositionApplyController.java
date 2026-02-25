package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.PositionApplyExportRow;
import com.liyao.bulk.dto.PositionApplyQueryRequest;
import com.liyao.bulk.dto.PositionApplyReviewRequest;
import com.liyao.bulk.dto.PositionApplySummary;
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

    public PositionApplyController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @Operation(summary = "查询岗位申请", description = "按条件查询岗位申请列表")
    /**
     * 菜单: 内部管理-岗位管理-岗位申请管理-已复核查询/待复核查询
     * 功能: 查询岗位申请记录
     * 示例: /api/position-applications
     */
    public ApiResponse<List<PositionApplySummary>> queryApplies(PositionApplyQueryRequest request) {
        return ApiResponse.success(positionService.queryPositionApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出岗位申请", description = "导出岗位申请查询结果")
    /**
     * 菜单: 内部管理-岗位管理-岗位申请管理-下载
     * 功能: 导出岗位申请查询结果
     * 示例: /api/position-applications/export
     */
    public ResponseEntity<StreamingResponseBody> exportApplies(PositionApplyQueryRequest request) {
        List<PositionApplyExportRow> rows = positionService.buildApplyExport(request);
        String fileName = URLEncoder.encode("岗位申请信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, PositionApplyExportRow.class)
                .sheet("岗位申请信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销岗位申请", description = "撤销指定的岗位申请")
    /**
     * 菜单: 内部管理-岗位管理-岗位申请管理-撤销
     * 功能: 撤销待复核申请
     * 示例: /api/position-applications/{id}/revoke
     */
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @Parameter(description = "操作人ID") @RequestParam Long operatorId) {
        positionService.revokeApply(id, operatorId);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "审核岗位申请", description = "审核岗位申请并提交结果")
    /**
     * 菜单: 内部管理-岗位管理-岗位申请管理-录入复核
     * 功能: 复核岗位申请
     * 示例: /api/position-applications/{id}/review
     */
    public ApiResponse<Void> reviewApply(@Parameter(description = "申请ID") @PathVariable Long id,
                                         @RequestBody PositionApplyReviewRequest request) {
        positionService.reviewApply(id, request);
        return ApiResponse.success();
    }
}
