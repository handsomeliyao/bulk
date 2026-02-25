package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import java.util.Objects;

public final class ApplyReviewValidationSupport {

    private ApplyReviewValidationSupport() {
    }

    public static void validateBaseFields(String applyOperCode,
                                          String requestOperCode,
                                          String applyOperName,
                                          String requestOperName,
                                          String applyTelPhone,
                                          String requestTelPhone,
                                          String applyPhone,
                                          String requestPhone,
                                          String applyRemark,
                                          String requestRemark,
                                          String message) {
        if (requestOperCode != null && !Objects.equals(applyOperCode, requestOperCode)) {
            throw new BusinessException(message);
        }
        if (!Objects.equals(applyOperName, requestOperName)
                || !Objects.equals(applyTelPhone, requestTelPhone)
                || !Objects.equals(applyPhone, requestPhone)
                || !Objects.equals(applyRemark, requestRemark)) {
            throw new BusinessException(message);
        }
    }
}
