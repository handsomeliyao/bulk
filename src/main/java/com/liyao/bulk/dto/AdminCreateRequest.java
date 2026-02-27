package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class AdminCreateRequest {
    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "用户ID")
    private String operCode;

    @Schema(description = "用户姓名")
    private String operName;

    @Schema(description = "办公电话")
    private String telPhone;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "用户状态")
    private String operStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "授权权限按钮ID列表")
    private List<String> assignAuth;

    @Schema(description = "操作权限按钮ID列表")
    private List<String> operAuth;
}
