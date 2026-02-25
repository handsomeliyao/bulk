package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Department {
    private Long id;
    private String deptName;
    private String remark;
    private String deptStatus;
    private String createdOperName;
    private LocalDateTime createdAt;
    private String updatedOperName;
    private LocalDateTime updatedAt;
    private String reviewOperName;
    private LocalDateTime reviewTime;
}
