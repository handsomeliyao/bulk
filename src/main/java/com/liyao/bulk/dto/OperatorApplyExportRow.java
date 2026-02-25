package com.liyao.bulk.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OperatorApplyExportRow {
    @ExcelProperty("")
    @Schema(description = "申请单号")
    private String arrNo;

    @ExcelProperty("")
    @Schema(description = "申请人ID")
    private Long arrOperCode;

    @ExcelProperty("")
    @Schema(description = "操作类型")
    private String operType;

    @ExcelProperty("")
    @Schema(description = "申请状态")
    private String operStatus;

    @ExcelProperty("")
    @Schema(description = "申请人姓名")
    private String arrOperName;

    @ExcelProperty("")
    @Schema(description = "申请时间")
    private LocalDateTime arrDate;

    @ExcelProperty("")
    @Schema(description = "复核人姓名")
    private String reviewOperName;

    @ExcelProperty("")
    @Schema(description = "复核人ID")
    private Long reviewOperCode;

    @ExcelProperty("")
    @Schema(description = "复核时间")
    private LocalDateTime reviewTime;
}
