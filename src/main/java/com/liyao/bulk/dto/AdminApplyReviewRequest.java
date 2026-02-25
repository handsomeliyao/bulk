package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdminApplyReviewRequest {
    @Schema(description = "是否通过")
    private boolean approved;
    @Schema(description = "复核人ID")
    private Long reviewOperCode;
    @Schema(description = "复核人姓名")
    private String reviewOperName;
    @Schema(description = "复核意见")
    private String reviewRemark;
    @Schema(description = "用户ID")
    private String operCode;
    @Schema(description = "用户姓名")
    private String operName;
    @Schema(description = "办公电话")
    private String telPhone;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "备注")
    private String remark;
}
