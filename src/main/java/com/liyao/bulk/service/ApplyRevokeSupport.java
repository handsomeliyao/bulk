package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import java.util.Objects;
import java.util.function.Function;

public final class ApplyRevokeSupport {

    private ApplyRevokeSupport() {
    }

    public interface RevokeStatusUpdater {
        void update(Long applyId, String status, Long reviewerId, String reviewerName,
                    java.time.LocalDateTime reviewTime, String reviewRemark);
    }

    public static <A> void revokeApply(
            Long applyId,
            Object operatorId,
            Function<Long, A> findApply,
            Function<A, String> statusGetter,
            Function<A, ?> applicantIdGetter,
            String pendingStatus,
            String canceledStatus,
            RevokeStatusUpdater statusUpdater
    ) {
        A apply = findApply.apply(applyId);
        if (!pendingStatus.equals(statusGetter.apply(apply))) {
            throw new BusinessException("申请记录状态不可进行撤销操作，请查证后重新操作。");
        }
        if (!Objects.equals(applicantIdGetter.apply(apply), operatorId)) {
            throw new BusinessException("操作用户仅能撤销本人提交的申请。");
        }
        statusUpdater.update(applyId, canceledStatus, null, null, null, null);
    }
}
