package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class PositionCreateRequest {
    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "岗位名称")
    private String postName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作范围")
    private List<PositionScopeItem> operScopes;
}
