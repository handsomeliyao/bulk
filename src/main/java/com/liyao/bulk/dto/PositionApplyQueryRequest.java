package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PositionApplyQueryRequest {
    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "状态类型")
    private String statusType;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "申请编号")
    private String arrNo;

    @Schema(description = "岗位名称")
    private String postName;

    @Schema(description = "操作类型")
    private String operType;
}
