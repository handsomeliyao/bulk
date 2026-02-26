package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class DepartmentSummary {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门状态")
    private String deptStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String createdOperName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新人")
    private String updatedOperName;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "复核人姓名")
    private String reviewOperName;

    @Schema(description = "复核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "授权权限")
    private List<ButtonAuthItem> assignAuth;

    @Schema(description = "操作权限")
    private List<ButtonAuthItem> operAuth;
}