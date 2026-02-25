package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ApplyReviewSupport {

    private ApplyReviewSupport() {
    }

    public interface ReviewStatusUpdater {
        void update(Long applyId, String status, Long reviewerId, String reviewerName,
                    LocalDateTime reviewTime, String reviewRemark);
    }

    public static <A, R> void reviewApply(
            Long applyId,
            R request,
            Function<Long, A> findApply,
            Function<A, String> statusGetter,
            Function<A, ?> applicantIdGetter,
            String pendingStatus,
            String rejectedStatus,
            String approvedStatus,
            BooleanSupplier inputReviewMode,
            Function<A, String> operationTypeGetter,
            Predicate<String> requiresInputReview,
            BiConsumer<A, R> ensureReviewMatch,
            BiConsumer<A, R> applyApproval,
            ReviewStatusUpdater statusUpdater,
            Function<R, Boolean> approvedGetter,
            Function<R, Long> reviewerIdGetter,
            Function<R, String> reviewerNameGetter,
            Function<R, String> reviewRemarkGetter
    ) {
        A apply = findApply.apply(applyId);
        if (!pendingStatus.equals(statusGetter.apply(apply))) {
            throw new BusinessException("仅能对待复核状态数据进行复核操作，请重新选择记录进行复核操作。");
        }
        if (Objects.equals(applicantIdGetter.apply(apply), reviewerIdGetter.apply(request))) {
            throw new BusinessException("不能复核自己提交的申请记录。");
        }
        if (!approvedGetter.apply(request)) {
            statusUpdater.update(applyId, rejectedStatus,
                    reviewerIdGetter.apply(request),
                    reviewerNameGetter.apply(request),
                    LocalDateTime.now(),
                    reviewRemarkGetter.apply(request));
            return;
        }
        if (inputReviewMode.getAsBoolean() && requiresInputReview.test(operationTypeGetter.apply(apply))) {
            ensureReviewMatch.accept(apply, request);
        }
        applyApproval.accept(apply, request);
        statusUpdater.update(applyId, approvedStatus,
                reviewerIdGetter.apply(request),
                reviewerNameGetter.apply(request),
                LocalDateTime.now(),
                reviewRemarkGetter.apply(request));
    }
}
