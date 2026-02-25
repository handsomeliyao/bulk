package com.liyao.bulk.model;

import java.time.LocalDateTime;

public interface ApplicantAwareApply {
    void setOperCode(Long applicantId);

    void setOperName(String applicantName);

    void setArrDate(LocalDateTime applyTime);
}
