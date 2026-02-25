package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.DepartmentApplyApproveRequest;
import com.liyao.bulk.dto.DepartmentApplyExportRow;
import com.liyao.bulk.dto.DepartmentApplyQueryRequest;
import com.liyao.bulk.dto.DepartmentApplySummary;
import com.liyao.bulk.service.DepartmentService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/department-applications")
public class DepartmentApplyController {

    private final DepartmentService departmentService;

    public DepartmentApplyController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "查询部门申请", description = "按条件查询部门申请列表")
    public ApiResponse<List<DepartmentApplySummary>> queryApplies(DepartmentApplyQueryRequest request) {
        return ApiResponse.success(departmentService.queryDepartmentApplies(request));
    }

    @GetMapping("/export")
    @Operation(summary = "导出部门申请", description = "导出部门申请查询结果")
    public ResponseEntity<StreamingResponseBody> exportApplies(DepartmentApplyQueryRequest request) {
        List<DepartmentApplyExportRow> rows = departmentService.buildApplyExport(request);
        String fileName = URLEncoder.encode("部门申请信息.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, DepartmentApplyExportRow.class)
                .sheet("部门申请信息")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销部门申请", description = "撤销指定的部门申请")
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id) {
        departmentService.revokeApply(id);
        return ApiResponse.success();
    }

    @PostMapping("/review")
    @Operation(summary = "审核部门申请", description = "按申请ID审核通过，并落库正式部门")
    public ApiResponse<Void> reviewApply(@RequestBody DepartmentApplyApproveRequest request) {
        departmentService.reviewApply(request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "审核部门申请(兼容)", description = "兼容旧路径，path id 与 body.applyId 需一致")
    public ApiResponse<Void> reviewApplyByPath(@Parameter(description = "申请ID") @PathVariable Long id,
                                               @RequestBody DepartmentApplyApproveRequest request) {
        if (request.getApplyId() == null) {
            request.setApplyId(id);
        } else if (!id.equals(request.getApplyId())) {
            return ApiResponse.error("path id and applyId are inconsistent");
        }
        departmentService.reviewApply(request);
        return ApiResponse.success();
    }
}