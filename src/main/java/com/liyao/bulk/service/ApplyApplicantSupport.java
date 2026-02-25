package com.liyao.bulk.service;

import com.liyao.bulk.dto.ApplicantInfo;
import com.liyao.bulk.model.ApplicantAwareApply;
import java.time.LocalDateTime;

public final class ApplyApplicantSupport {

    private ApplyApplicantSupport() {
    }

    public static void fillApplicantInfo(ApplicantAwareApply apply, ApplicantInfo info) {
        apply.setOperCode(info.getOperCode());
        apply.setOperName(info.getOperName());
        apply.setArrDate(LocalDateTime.now());
    }
}
