package com.liyao.bulk.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.liyao.bulk.common.ApiResponse;
import com.liyao.bulk.dto.PositionCancelRequest;
import com.liyao.bulk.dto.PositionCreateRequest;
import com.liyao.bulk.dto.PositionDetailResponse;
import com.liyao.bulk.dto.PositionExportRow;
import com.liyao.bulk.dto.PositionModifyRequest;
import com.liyao.bulk.dto.PositionSummary;
import com.liyao.bulk.dto.UserSummary;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/internal-positions")
public class InternalPositionController {

    private final PositionService positionService;

    public InternalPositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @Operation(summary = "查询内部岗位", description = "按条件查询内部岗位列表")
    public ApiResponse<List<PositionSummary>> queryPositions(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "岗位名称") @RequestParam(required = false) String postName,
            @Parameter(description = "岗位状态") @RequestParam(required = false) String postStatus) {
        return ApiResponse.success(positionService.queryInternalPositions(deptId, postName, postStatus));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询内部岗位详情", description = "根据岗位ID获取内部岗位详情")
    public ApiResponse<PositionDetailResponse> getPositionDetail(@Parameter(description = "岗位ID") @PathVariable Long id) {
        return ApiResponse.success(positionService.getPositionDetail(id));
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "查询岗位用户", description = "查询内部岗位绑定的用户列表")
    public ApiResponse<List<UserSummary>> queryPositionUsers(@Parameter(description = "岗位ID") @PathVariable Long id) {
        return ApiResponse.success(positionService.queryPositionUsers(id));
    }

    @GetMapping("/export")
    @Operation(summary = "导出内部岗位", description = "导出内部岗位查询结果")
    public ResponseEntity<StreamingResponseBody> exportPositions(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "岗位名称") @RequestParam(required = false) String postName,
            @Parameter(description = "岗位状态") @RequestParam(required = false) String postStatus) {
        List<PositionExportRow> rows = positionService.buildPositionExport(deptId, postName, postStatus);
        String fileName = URLEncoder.encode("内部岗位管理.xlsx", StandardCharsets.UTF_8);
        StreamingResponseBody body = outputStream -> EasyExcelFactory.write(outputStream, PositionExportRow.class)
                .sheet("内部岗位管理")
                .doWrite(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/applications")
    @Operation(summary = "新增内部岗位申请", description = "提交新增内部岗位申请")
    public ApiResponse<Void> createPosition(@RequestBody PositionCreateRequest request) {
        positionService.createPositionApply(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/applications")
    @Operation(summary = "修改内部岗位申请", description = "修改内部岗位申请信息")
    public ApiResponse<Void> modifyPosition(@Parameter(description = "申请ID") @PathVariable Long id,
                                            @RequestBody PositionModifyRequest request) {
        positionService.modifyPositionApply(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/applications/cancel")
    @Operation(summary = "注销内部岗位申请", description = "提交注销内部岗位申请")
    public ApiResponse<Void> cancelPosition(@Parameter(description = "申请ID") @PathVariable Long id,
                                            @RequestBody PositionCancelRequest request) {
        positionService.cancelPositionApply(id, request);
        return ApiResponse.success();
    }
}
