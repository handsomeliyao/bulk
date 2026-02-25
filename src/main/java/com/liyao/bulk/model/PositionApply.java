package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PositionApply implements ApplicantAwareApply {
    private Long id;
    private String arrNo;
    private Long postId;
    private Long deptId;
    private String deptName;
    private String postName;
    private String remark;
    private String postType;
    private String operType;
    private String postStatus;
    private Long operCode;
    private String operName;
    private LocalDateTime arrDate;
    private Long reviewOperCode;
    private String reviewOperName;
    private LocalDateTime reviewTime;
}
