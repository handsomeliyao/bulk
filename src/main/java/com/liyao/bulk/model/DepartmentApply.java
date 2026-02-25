package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepartmentApply {
    private Long id;
    private String arrNo;
    private Long deptId;
    private String deptName;
    private String remark;
    private String operType;
    // 申请状态枚举：PENDING(待复核)、REJECTED(复核拒绝)、CANCELED(已撤销)
    private String operStatus;
    private String operCode;
    private String operName;
    private LocalDateTime arrDate;
    private Long reviewOperCode;
    private String reviewOperName;
    private LocalDateTime reviewTime;
    private String deptStatus;
}
