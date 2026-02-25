package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserSummary {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private String operCode;
    @Schema(description = "用户姓名")
    private String operName;
    @Schema(description = "状态")
    private String operStatus;
    @Schema(description = "用户类型")
    private String userType;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "邮箱")
    private String email;
}
