package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PlatformUser {
    private Long id;
    private String operCode;
    private String password;
    private String operName;
    private String operStatus;
    private String userType;
    private String telPhone;
    private String phone;
    private String remark;
    private Long deptId;
    private String createdOperName;
    private LocalDateTime createdAt;
    private String updatedOperName;
    private LocalDateTime updatedAt;
    private String reviewOperName;
    private LocalDateTime reviewTime;
}
