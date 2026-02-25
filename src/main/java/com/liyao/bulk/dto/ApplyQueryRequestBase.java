package com.liyao.bulk.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public interface ApplyQueryRequestBase {
    Long getDeptId();

    String getStatusType();

    String getStartDate();

    String getEndDate();

    String getArrNo();

    String getArrOperName();

    String getOperType();
}
