package com.liyao.bulk.dto;

import java.time.LocalDateTime;

public interface UserDetailBase {
    void setId(Long id);

    void setDeptId(Long deptId);

    void setDeptName(String deptName);

    void setOperCode(String operCode);

    void setOperName(String operName);

    void setUserType(String userType);

    void setOperStatus(String operStatus);

    void setTelPhone(String telPhone);

    void setPhone(String phone);

    void setRemark(String remark);

    void setCreatedOperName(String createdOperName);

    void setCreatedAt(LocalDateTime createdAt);

    void setUpdatedOperName(String updatedOperName);

    void setUpdatedAt(LocalDateTime updatedAt);

    void setReviewOperName(String reviewOperName);

    void setReviewTime(LocalDateTime reviewTime);
}
