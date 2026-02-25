package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.DepartmentCancelRequest;
import com.liyao.bulk.dto.DepartmentCreateRequest;
import com.liyao.bulk.dto.DepartmentDetailResponse;
import com.liyao.bulk.dto.DepartmentExportRow;
import com.liyao.bulk.dto.DepartmentModifyRequest;
import com.liyao.bulk.dto.DepartmentSummary;
import com.liyao.bulk.dto.UserSummary;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "查询部门", description = "按名称和状态查询部门列表")
    public ApiResponse<List<DepartmentSummary>> queryDepartments(
            @Parameter(description = "部门名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        return ApiResponse.success(departmentService.queryDepartments(name, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询部门详情", description = "根据部门ID获取部门详情")
    public ApiResponse<DepartmentDetailResponse> getDepartmentDetail(@Parameter(description = "部门ID") @PathVariable Long id) {
        return ApiResponse.success(departmentService.getDepartmentDetail(id));
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "查询部门用户", description = "查询部门下用户列表")
    public ApiResponse<List<UserSummary>> queryDepartmentUsers(@Parameter(description = "部门ID") @PathVariable Long id) {
        return ApiResponse.success(departmentService.queryDepartmentUsers(id));
    }

    @GetMapping("/export")
    @Operation(summary = "导出部门", description = "导出部门查询结果")
    public ResponseEntity<StreamingResponseBody> exportDepartments(
            @Parameter(description = "部门名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        List<DepartmentExportRow> rows = departmentService.buildDepartmentExport(name, status);
        String fileName = URLEncoder.encode("部门管理.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, DepartmentExportRow.class)
                .sheet("部门管理")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/applications")
    @Operation(summary = "新增部门申请", description = "提交新增部门申请")
    public ApiResponse<Void> createDepartment(@RequestBody DepartmentCreateRequest request) {
        departmentService.createDepartmentApply(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/applications")
    @Operation(summary = "修改部门申请", description = "提交修改部门申请")
    public ApiResponse<Void> modifyDepartment(@Parameter(description = "部门ID") @PathVariable Long id,
                                              @RequestBody DepartmentModifyRequest request) {
        departmentService.modifyDepartmentApply(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/cancel")
    @Operation(summary = "注销部门申请", description = "提交注销部门申请")
    public ApiResponse<Void> cancelDepartment(@Parameter(description = "部门ID") @PathVariable Long id,
                                              @RequestBody(required = false) DepartmentCancelRequest request) {
        departmentService.cancelDepartmentApply(id);
        return ApiResponse.success();
    }
}
