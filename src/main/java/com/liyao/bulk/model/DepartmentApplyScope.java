package com.liyao.bulk.model;

import lombok.Data;

@Data
public class DepartmentApplyScope {
    private Long id;
    private Long applyId;
    private String scopeType;
    private String scopeCode;
    private String scopeName;
}
