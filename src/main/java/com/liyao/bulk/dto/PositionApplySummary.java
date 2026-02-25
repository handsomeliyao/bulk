package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PositionApplySummary {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "申请单号")
    private String arrNo;

    @Schema(description = "岗位ID")
    private Long postId;

    @Schema(description = "岗位名称")
    private String postName;

    @Schema(description = "操作类型")
    private String operType;

    @Schema(description = "岗位状态")
    private String postStatus;

    @Schema(description = "申请人姓名")
    private String arrOperName;

    @Schema(description = "申请时间")
    private LocalDateTime arrDate;

    @Schema(description = "复核人姓名")
    private String reviewOperName;

    @Schema(description = "复核人ID")
    private Long reviewOperCode;

    @Schema(description = "复核时间")
    private LocalDateTime reviewTime;
}
