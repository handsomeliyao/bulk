package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.DepartmentApplyApproveRequest;
import com.liyao.bulk.dto.DepartmentApplyDetailResponse;
import com.liyao.bulk.dto.DepartmentApplyExportRow;
import com.liyao.bulk.dto.DepartmentApplyQueryRequest;
import com.liyao.bulk.dto.DepartmentApplySummary;
import com.liyao.bulk.dto.DepartmentDetailResponse;
import com.liyao.bulk.service.ApplyCompareService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/department-applications")
public class DepartmentApplyController {

    private final DepartmentService departmentService;
    private final ApplyCompareService applyCompareService;

    public DepartmentApplyController(DepartmentService departmentService,
                                     ApplyCompareService applyCompareService) {
        this.departmentService = departmentService;
        this.applyCompareService = applyCompareService;
    }

    @GetMapping("/pending")
    @Operation(summary = "查询部门待复核申请", description = "按条件查询部门申请中待复核的数据")
    public ApiResponse<List<DepartmentApplySummary>> queryPendingApplies(DepartmentApplyQueryRequest request) {
        return ApiResponse.success(departmentService.queryPendingDepartmentApplies(request));
    }

    @GetMapping("/reviewed")
    @Operation(summary = "查询部门已复核申请", description = "按条件查询部门申请中已复核的数据")
    public ApiResponse<List<DepartmentApplySummary>> queryReviewedApplies(DepartmentApplyQueryRequest request) {
        return ApiResponse.success(departmentService.queryReviewedDepartmentApplies(request));
    }

    @GetMapping("/detail")
    @Operation(summary = "根据申请编号查询详情", description = "根据申请编号查询部门申请详情（包含权限数据）")
    public ApiResponse<DepartmentApplyDetailResponse> getApplyDetailByArrNo(
            @Parameter(description = "申请编号") @RequestParam String arrNo) {
        return ApiResponse.success(departmentService.getApplyDetailByArrNo(arrNo));
    }

    @GetMapping("/export")
    @Operation(summary = "导出部门申请", description = "导出部门申请列表为 Excel")
    public ResponseEntity<StreamingResponseBody> exportApplies(DepartmentApplyQueryRequest request) {
        List<DepartmentApplyExportRow> rows = departmentService.buildApplyExport(request);
        String fileName = URLEncoder.encode("部门申请导出.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, DepartmentApplyExportRow.class)
                .sheet("部门申请导出")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "撤销部门申请", description = "撤销指定部门申请")
    public ApiResponse<Void> revokeApply(@Parameter(description = "申请ID") @PathVariable Long id) {
        departmentService.revokeApply(id);
        return ApiResponse.success();
    }

    @PostMapping("/review")
    @Operation(summary = "复核部门申请", description = "根据请求体中的申请ID进行复核")
    public ApiResponse<Void> reviewApply(@RequestBody DepartmentApplyApproveRequest request) {
        departmentService.reviewApply(request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "按路径ID复核部门申请", description = "路径ID与请求体applyId需一致，如请求体未传则自动使用路径ID")
    public ApiResponse<Void> reviewApplyByPath(@Parameter(description = "申请ID") @PathVariable Long id,
                                               @RequestBody(required = false) DepartmentApplyApproveRequest request) {
        if (request == null) {
            request = new DepartmentApplyApproveRequest();
        }
        if (request.getApplyId() == null) {
            request.setApplyId(id);
        } else if (!id.equals(request.getApplyId())) {
            return ApiResponse.error("path id and applyId are inconsistent");
        }
        departmentService.reviewApply(request);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/compare")
    @Operation(summary = "比对部门申请与正式数据", description = "返回未修改字段的正式值，已修改字段返回空")
    public ApiResponse<DepartmentDetailResponse> compareApply(
            @Parameter(description = "申请ID或申请编号") @PathVariable String id) {
        return ApiResponse.success(applyCompareService.compareDepartment(id));
    }
}
