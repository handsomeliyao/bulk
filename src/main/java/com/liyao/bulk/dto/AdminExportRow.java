package com.liyao.bulk.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminExportRow {
    @ExcelProperty("")
    @Schema(description = "主键ID")
    private Long id;

    @ExcelProperty("")
    @Schema(description = "用户ID")
    private String operCode;

    @ExcelProperty("")
    @Schema(description = "用户姓名")
    private String operName;

    @ExcelProperty("")
    @Schema(description = "用户类型")
    private String userType;

    @ExcelProperty("")
    @Schema(description = "状态")
    private String operStatus;

    @ExcelProperty("")
    @Schema(description = "办公电话")
    private String telPhone;

    @ExcelProperty("")
    @Schema(description = "手机号")
    private String phone;

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
