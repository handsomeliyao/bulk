package com.liyao.bulk.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepartmentExportRow {
    @ExcelProperty("")
    @Schema(description = "主键ID")
    private Long id;

    @ExcelProperty("")
    @Schema(description = "部门名称")
    private String deptName;

    @ExcelProperty("")
    @Schema(description = "部门状态")
    private String deptStatus;

    @ExcelProperty("")
    @Schema(description = "备注")
    private String remark;

    @ExcelProperty("")
    @Schema(description = "创建人")
    private String createdOperName;

    @ExcelProperty("")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @ExcelProperty("")
    @Schema(description = "更新人")
    private String updatedOperName;

    @ExcelProperty("")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @ExcelProperty("")
    @Schema(description = "复核人姓名")
    private String reviewOperName;

    @ExcelProperty("")
    @Schema(description = "复核时间")
    private LocalDateTime reviewTime;
}
