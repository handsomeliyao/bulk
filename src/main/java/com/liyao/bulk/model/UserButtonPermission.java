package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserButtonPermission {
    private Long id;
    private Long userId;
    private Long btnId;
    private String permissionType;
    private LocalDateTime createdAt;
}
