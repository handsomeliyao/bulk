package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public interface ApplicantInfo {
    Long getOperCode();

    String getOperName();

    Long getApplicantDeptId();

    String getApplicantDeptName();
}
