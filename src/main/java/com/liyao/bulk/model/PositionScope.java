package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PositionScope {
    private Long id;
    private Long positionId;
    private String systemCode;
    private String systemName;
    private String scopeCode;
    private String scopeName;
    private LocalDateTime createdAt;
}
