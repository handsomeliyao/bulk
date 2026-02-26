package com.liyao.bulk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OperatorSummary {
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private String operCode;

    @Schema(description = "用户姓名")
    private String operName;

    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "状态")
    private String operStatus;

    @Schema(description = "办公电话")
    private String telPhone;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String createdOperName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新人")
    private String updatedOperName;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "复核人姓名")
    private String reviewOperName;

    @Schema(description = "复核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
}
