package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepartmentButtonPermission {
    private Long id;
    private Long deptId;
    private Long btnId;
    private String permissionType;
    private LocalDateTime createdAt;
}
