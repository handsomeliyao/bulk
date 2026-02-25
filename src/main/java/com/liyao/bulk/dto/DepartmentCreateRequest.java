package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class DepartmentCreateRequest {
    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "授权范围")
    private List<ButtonAuthItem> assignAuth;

    @Schema(description = "操作范围")
    private List<ButtonAuthItem> operAuth;
}
