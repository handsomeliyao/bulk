package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DepartmentOption {
    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "部门名称")
    private String deptName;
}
