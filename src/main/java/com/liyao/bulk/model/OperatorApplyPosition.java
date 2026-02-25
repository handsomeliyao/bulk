package com.liyao.bulk.model;

import lombok.Data;

@Data
public class OperatorApplyPosition {
    private Long id;
    private Long applyId;
    private Long positionId;
    private String positionName;
}
