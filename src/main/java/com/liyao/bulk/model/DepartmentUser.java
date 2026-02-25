package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepartmentUser {
    private Long id;
    private Long deptId;
    private Long userId;
    private LocalDateTime createdAt;
}
