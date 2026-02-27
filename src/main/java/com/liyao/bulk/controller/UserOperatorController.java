package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.OperatorActionRequest;
import com.liyao.bulk.dto.OperatorCreateRequest;
import com.liyao.bulk.dto.OperatorDetailResponse;
import com.liyao.bulk.dto.OperatorExportRow;
import com.liyao.bulk.dto.OperatorModifyRequest;
import com.liyao.bulk.dto.OperatorSummary;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/user-operators")
public class UserOperatorController {

    private final OperatorService operatorService;

    public UserOperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @GetMapping
    @Operation(summary = "查询用户操作员")
    public ApiResponse<List<OperatorSummary>> queryOperators(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "用户编码") @RequestParam(required = false) String operCode,
            @Parameter(description = "用户名称") @RequestParam(required = false) String operName,
            @Parameter(description = "用户状态") @RequestParam(required = false) String operStatus) {
        return ApiResponse.success(operatorService.queryOperators(deptId, operCode, operName, operStatus));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户操作员详情")
    public ApiResponse<OperatorDetailResponse> getOperatorDetail(@Parameter(description = "用户ID") @PathVariable Long id,
                                                                 @Parameter(description = "部门ID") @RequestParam Long deptId) {
        return ApiResponse.success(operatorService.getOperatorDetail(id, deptId));
    }

    @GetMapping("/export")
    @Operation(summary = "导出用户操作员")
    public ResponseEntity<StreamingResponseBody> exportOperators(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "用户编码") @RequestParam(required = false) String operCode,
            @Parameter(description = "用户名称") @RequestParam(required = false) String operName,
            @Parameter(description = "用户状态") @RequestParam(required = false) String operStatus) {
        List<OperatorExportRow> rows = operatorService.buildOperatorExport(deptId, operCode, operName, operStatus);
        String fileName = URLEncoder.encode("用户操作员信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, OperatorExportRow.class)
                .sheet("用户操作员信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/applications")
    @Operation(summary = "新增用户操作员申请")
    public ApiResponse<Void> createOperator(@RequestBody OperatorCreateRequest request) {
        operatorService.createOperatorApply(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/applications")
    @Operation(summary = "修改用户操作员申请")
    public ApiResponse<Void> modifyOperator(@Parameter(description = "用户ID") @PathVariable Long id,
                                            @RequestBody OperatorModifyRequest request) {
        operatorService.modifyOperatorApply(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/cancel")
    @Operation(summary = "注销用户操作员申请")
    public ApiResponse<Void> cancelOperator(@Parameter(description = "用户ID") @PathVariable Long id,
                                            @RequestBody(required = false) OperatorActionRequest request) {
        operatorService.cancelOperator(id, request == null ? new OperatorActionRequest() : request);
        return ApiResponse.success();
    }
}
