package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepartmentScope {
    private Long id;
    private Long deptId;
    private String scopeType;
    private String scopeCode;
    private String scopeName;
    private LocalDateTime createdAt;
}
