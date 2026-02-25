package com.liyao.bulk.service;

import com.liyao.bulk.dto.ApplicantInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentLoginUser implements ApplicantInfo {
    private Long operCode;
    private String operName;
    private Long applicantDeptId;
    private String applicantDeptName;
    private String loginOperCode;
}
