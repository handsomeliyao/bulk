package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Position {
    private Long id;
    private Long deptId;
    private String deptName;
    private String postName;
    private String postType;
    private String remark;
    private String postStatus;
    private String createdOperName;
    private LocalDateTime createdAt;
    private String updatedOperName;
    private LocalDateTime updatedAt;
    private String reviewOperName;
    private LocalDateTime reviewTime;
}
