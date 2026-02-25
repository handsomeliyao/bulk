package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import java.util.List;

public final class ApplyStatusSupport {

    private ApplyStatusSupport() {
    }

    public static List<String> resolve(String statusType,
                                       String approved,
                                       String pending,
                                       String rejected,
                                       String canceled) {
        if (statusType == null || statusType.isBlank()) {
            return List.of(approved, pending, rejected, canceled);
        }
        if ("REVIEWED".equalsIgnoreCase(statusType)) {
            return List.of(approved, rejected, canceled);
        }
        if ("PENDING".equalsIgnoreCase(statusType)) {
            return List.of(pending);
        }
        if ("APPROVED".equalsIgnoreCase(statusType)) {
            return List.of(approved);
        }
        if ("REJECTED".equalsIgnoreCase(statusType)) {
            return List.of(rejected);
        }
        if ("CANCELED".equalsIgnoreCase(statusType)) {
            return List.of(canceled);
        }
        throw new BusinessException("无效的查询状态类型");
    }
}