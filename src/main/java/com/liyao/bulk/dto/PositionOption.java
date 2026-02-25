package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PositionOption {
    @Schema(description = "岗位ID")
    private Long postId;
    @Schema(description = "岗位名称")
    private String postName;
    @Schema(description = "是否选中")
    private boolean selected;
}
