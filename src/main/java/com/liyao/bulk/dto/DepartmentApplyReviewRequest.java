package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class DepartmentApplyReviewRequest {
    @Schema(description = "是否通过")
    private boolean approved;

    @Schema(description = "复核人ID")
    private Long reviewOperCode;

    @Schema(description = "复核人姓名")
    private String reviewOperName;

    @Schema(description = "复核意见")
    private String reviewRemark;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门备注")
    private String deptRemark;

    @Schema(description = "授权权限")
    private List<ButtonAuthItem> assignAuth;

    @Schema(description = "操作权限")
    private List<ButtonAuthItem> operAuth;
}