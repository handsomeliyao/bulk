package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;

public final class UserCreateValidationSupport {

    private UserCreateValidationSupport() {
    }

    public static void validate(Long deptId, String operCode, String operName) {
        if (operCode == null || operCode.trim().isEmpty()) {
            throw new BusinessException("请录入用户名。");
        }
        if (operName == null || operName.trim().isEmpty()) {
            throw new BusinessException("请录入用户姓名。");
        }
    }
}