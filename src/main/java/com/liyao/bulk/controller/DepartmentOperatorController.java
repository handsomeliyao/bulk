package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.OperatorActionRequest;
import com.liyao.bulk.dto.OperatorAssignPermissionRequest;
import com.liyao.bulk.dto.OperatorCreateRequest;
import com.liyao.bulk.dto.OperatorDetailResponse;
import com.liyao.bulk.dto.OperatorExportRow;
import com.liyao.bulk.dto.OperatorModifyRequest;
import com.liyao.bulk.dto.OperatorPermissionItem;
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
@RequestMapping("/api/department-operators")
public class DepartmentOperatorController {

    private final OperatorService operatorService;

    public DepartmentOperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @GetMapping
    @Operation(summary = "查询部门操作员", description = "按条件查询部门操作员列表")
    public ApiResponse<List<OperatorSummary>> queryOperators(
            @Parameter(description = "部门ID") @RequestParam Long deptId,
            @Parameter(description = "用户编码") @RequestParam(required = false) String operCode,
            @Parameter(description = "用户姓名") @RequestParam(required = false) String operName,
            @Parameter(description = "用户状态") @RequestParam(required = false) String operStatus) {
        return ApiResponse.success(operatorService.queryOperators(deptId, operCode, operName, operStatus));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询操作员详情", description = "根据操作员ID获取详情")
    public ApiResponse<OperatorDetailResponse> getOperatorDetail(@Parameter(description = "操作员ID") @PathVariable Long id,
                                                                 @Parameter(description = "部门ID") @RequestParam Long deptId) {
        return ApiResponse.success(operatorService.getOperatorDetail(id, deptId));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "刷新操作员权限", description = "刷新并返回操作员权限列表")
    public ApiResponse<List<OperatorPermissionItem>> refreshPermissions(@Parameter(description = "操作员ID") @PathVariable Long id,
                                                                        @Parameter(description = "部门ID") @RequestParam Long deptId) {
        return ApiResponse.success(operatorService.refreshPermissions(id, deptId));
    }

    @GetMapping("/export")
    @Operation(summary = "导出部门操作员", description = "导出部门操作员查询结果")
    public ResponseEntity<StreamingResponseBody> exportOperators(
            @Parameter(description = "部门ID") @RequestParam Long deptId,
            @Parameter(description = "用户编码") @RequestParam(required = false) String operCode,
            @Parameter(description = "用户姓名") @RequestParam(required = false) String operName,
            @Parameter(description = "用户状态") @RequestParam(required = false) String operStatus) {
        List<OperatorExportRow> rows = operatorService.buildOperatorExport(deptId, operCode, operName, operStatus);
        String fileName = URLEncoder.encode("部门操作员信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, OperatorExportRow.class)
                .sheet("部门操作员信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/applications")
    @Operation(summary = "新增操作员申请", description = "提交新增部门操作员申请")
    public ApiResponse<Void> createOperator(@RequestBody OperatorCreateRequest request) {
        operatorService.createOperatorApply(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/applications")
    @Operation(summary = "修改操作员申请", description = "修改部门操作员申请信息")
    public ApiResponse<Void> modifyOperator(@Parameter(description = "申请ID") @PathVariable Long id,
                                            @RequestBody OperatorModifyRequest request) {
        operatorService.modifyOperatorApply(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/freeze")
    @Operation(summary = "冻结操作员申请", description = "提交冻结部门操作员申请")
    public ApiResponse<Void> freezeOperator(@Parameter(description = "申请ID") @PathVariable Long id,
                                            @RequestBody OperatorActionRequest request) {
        operatorService.freezeOperator(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/unfreeze")
    @Operation(summary = "解冻操作员申请", description = "提交解冻部门操作员申请")
    public ApiResponse<Void> unfreezeOperator(@Parameter(description = "申请ID") @PathVariable Long id,
                                              @RequestBody OperatorActionRequest request) {
        operatorService.unfreezeOperator(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/reset-password")
    @Operation(summary = "重置密码申请", description = "提交操作员重置密码申请")
    public ApiResponse<Void> resetPassword(@Parameter(description = "申请ID") @PathVariable Long id,
                                           @RequestBody OperatorActionRequest request) {
        operatorService.resetPassword(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/cancel")
    @Operation(summary = "注销操作员申请", description = "提交注销部门操作员申请")
    public ApiResponse<Void> cancelOperator(@Parameter(description = "申请ID") @PathVariable Long id,
                                            @RequestBody OperatorActionRequest request) {
        operatorService.cancelOperator(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/assign-permissions")
    @Operation(summary = "分配权限申请", description = "提交操作员分配权限申请")
    public ApiResponse<Void> assignPermissions(@Parameter(description = "申请ID") @PathVariable Long id,
                                               @RequestBody OperatorAssignPermissionRequest request) {
        operatorService.assignPermissions(id, request);
        return ApiResponse.success();
    }
}
