package com.liyao.bulk.model;

import lombok.Data;

@Data
public class PositionApplyScope {
    private Long id;
    private Long applyId;
    private String systemCode;
    private String systemName;
    private String scopeCode;
    private String scopeName;
}
