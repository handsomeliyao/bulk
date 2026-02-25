package com.liyao.bulk.service;

import com.liyao.bulk.dto.UserDetailBase;
import com.liyao.bulk.model.PlatformUser;

public final class UserDetailAssembler {

    private UserDetailAssembler() {
    }

    public static void fillBase(UserDetailBase target, PlatformUser user, Long deptId, String deptName) {
        target.setId(user.getId());
        target.setDeptId(deptId);
        target.setDeptName(deptName);
        target.setOperCode(user.getOperCode());
        target.setOperName(user.getOperName());
        target.setUserType(user.getUserType());
        target.setOperStatus(user.getOperStatus());
        target.setTelPhone(user.getTelPhone());
        target.setPhone(user.getPhone());
        target.setRemark(user.getRemark());
        target.setCreatedOperName(user.getCreatedOperName());
        target.setCreatedAt(user.getCreatedAt());
        target.setUpdatedOperName(user.getUpdatedOperName());
        target.setUpdatedAt(user.getUpdatedAt());
        target.setReviewOperName(user.getReviewOperName());
        target.setReviewTime(user.getReviewTime());
    }
}

