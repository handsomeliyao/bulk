package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DepartmentApplyQueryRequest {
    @Schema(description = "申请状态类型：PENDING(待复核)、REJECTED(复核拒绝)、CANCELED(已撤销)")
    // 部门申请状态筛选枚举：待复核/复核拒绝/已撤销
    private String statusType;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "申请编号")
    private String arrNo;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "操作类型")
    private String operType;
}
