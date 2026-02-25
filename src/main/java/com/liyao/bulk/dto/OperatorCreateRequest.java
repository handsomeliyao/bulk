package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OperatorCreateRequest {
    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

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
