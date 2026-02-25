package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminApply implements ApplicantAwareApply {
    private Long id;
    private String arrNo;
    private Long deptId;
    private String operType;
    private String telPhone;
    private String mobile;
    private String remark;
    private String operationType;
    private String operStatus;
    private Long operCode;
    private String operName;
    private LocalDateTime arrDate;
    private Long reviewOperCode;
    private String reviewOperName;
    private LocalDateTime reviewTime;
}
