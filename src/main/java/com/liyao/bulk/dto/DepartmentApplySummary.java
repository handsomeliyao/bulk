package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepartmentApplySummary {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "申请编号")
    private String arrNo;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "申请备注")
    private String remark;

    @Schema(description = "操作类型")
    private String operType;

    @Schema(description = "申请状态：PENDING(待复核)、REJECTED(复核拒绝)、CANCELED(已撤销)")
    private String arrStatus;

    @Schema(description = "部门状态")
    private String deptStatus;

    @Schema(description = "申请人姓名")
    private String arrOperName;

    @Schema(description = "申请人编码")
    private String arrOperCode;

    @Schema(description = "申请时间")
    private LocalDateTime arrDate;

    @Schema(description = "复核人姓名")
    private String reviewOperName;

    @Schema(description = "复核人ID")
    private Long reviewOperCode;

    @Schema(description = "复核时间")
    private LocalDateTime reviewTime;
}
