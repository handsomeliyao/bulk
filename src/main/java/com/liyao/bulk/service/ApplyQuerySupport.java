package com.liyao.bulk.service;

import com.liyao.bulk.dto.ApplyQueryRequestBase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public final class ApplyQuerySupport {

    private ApplyQuerySupport() {
    }

    public interface ApplyQueryExecutor<T> {
        List<T> execute(Long deptId,
                        List<String> statuses,
                        LocalDateTime startTime,
                        LocalDateTime endTime,
                        String applyNo,
                        String operCode,
                        String operationType);
    }

    public interface ApplyQueryExecutorWithName<T> {
        List<T> execute(Long deptId,
                        List<String> statuses,
                        LocalDateTime startTime,
                        LocalDateTime endTime,
                        String applyNo,
                        String name,
                        String operationType);
    }

    public interface ApplyQueryExecutorNoDept<T> {
        List<T> execute(List<String> statuses,
                        LocalDateTime startTime,
                        LocalDateTime endTime,
                        String applyNo,
                        String name,
                        String operationType);
    }

    public static <T, R extends ApplyQueryRequestBase> List<T> queryApplies(
            R request,
            Function<String, List<String>> statusResolver,
            Function<String, LocalDateTime> startParser,
            Function<String, LocalDateTime> endParser,
            ApplyQueryExecutor<T> executor) {
        List<String> statuses = statusResolver.apply(request.getStatusType());
        return executor.execute(
                request.getDeptId(),
                statuses,
                startParser.apply(request.getStartDate()),
                endParser.apply(request.getEndDate()),
                request.getArrNo(),
                request.getArrOperName(),
                request.getOperType());
    }

    public static <T, R> List<T> queryAppliesWithName(
            R request,
            Function<R, Long> deptIdGetter,
            Function<R, String> statusTypeGetter,
            Function<R, String> startDateGetter,
            Function<R, String> endDateGetter,
            Function<R, String> applyNoGetter,
            Function<R, String> nameGetter,
            Function<R, String> operationTypeGetter,
            Function<String, List<String>> statusResolver,
            Function<String, LocalDateTime> startParser,
            Function<String, LocalDateTime> endParser,
            ApplyQueryExecutorWithName<T> executor) {
        Long deptId = deptIdGetter.apply(request);
        List<String> statuses = statusResolver.apply(statusTypeGetter.apply(request));
        return executor.execute(
                deptId,
                statuses,
                startParser.apply(startDateGetter.apply(request)),
                endParser.apply(endDateGetter.apply(request)),
                applyNoGetter.apply(request),
                nameGetter.apply(request),
                operationTypeGetter.apply(request));
    }

    public static <T, R> List<T> queryAppliesNoDept(
            R request,
            Function<R, String> statusTypeGetter,
            Function<R, String> startDateGetter,
            Function<R, String> endDateGetter,
            Function<R, String> applyNoGetter,
            Function<R, String> nameGetter,
            Function<R, String> operationTypeGetter,
            Function<String, List<String>> statusResolver,
            Function<String, LocalDateTime> startParser,
            Function<String, LocalDateTime> endParser,
            ApplyQueryExecutorNoDept<T> executor) {
        List<String> statuses = statusResolver.apply(statusTypeGetter.apply(request));
        return executor.execute(
                statuses,
                startParser.apply(startDateGetter.apply(request)),
                endParser.apply(endDateGetter.apply(request)),
                applyNoGetter.apply(request),
                nameGetter.apply(request),
                operationTypeGetter.apply(request));
    }
}
