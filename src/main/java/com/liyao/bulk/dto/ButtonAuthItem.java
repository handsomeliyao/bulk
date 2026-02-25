package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ButtonAuthItem {
    @Schema(description = "Button id")
    private Long btnId;
}
