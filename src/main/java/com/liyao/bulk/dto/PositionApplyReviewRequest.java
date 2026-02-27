package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class PositionApplyReviewRequest {
    @Schema(description = "是否通过")
    private boolean approved;

    @Schema(description = "复核人ID")
    private Long reviewOperCode;

    @Schema(description = "复核人姓名")
    private String reviewOperName;

    @Schema(description = "复核意见")
    private String reviewRemark;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作权限")
    private List<PositionScopeItem> operAuth;
}