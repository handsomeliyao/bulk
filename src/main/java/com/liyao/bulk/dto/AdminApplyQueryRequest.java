package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdminApplyQueryRequest implements ApplyQueryRequestBase {
    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "状态类型")
    private String statusType;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "申请单号")
    private String arrNo;

    @Schema(description = "申请人姓名")
    private String arrOperName;

    @Schema(description = "操作类型")
    private String operType;
}
