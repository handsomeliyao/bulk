package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.dto.PositionApplyExportRow;
import com.liyao.bulk.dto.PositionApplyQueryRequest;
import com.liyao.bulk.dto.PositionApplyReviewRequest;
import com.liyao.bulk.dto.PositionApplySummary;
import com.liyao.bulk.dto.PositionCancelRequest;
import com.liyao.bulk.dto.PositionCreateRequest;
import com.liyao.bulk.dto.PositionDetailResponse;
import com.liyao.bulk.dto.PositionExportRow;
import com.liyao.bulk.dto.PositionModifyRequest;
import com.liyao.bulk.dto.PositionSummary;
import com.liyao.bulk.dto.UserSummary;
import com.liyao.bulk.mapper.PlatformUserMapper;
import com.liyao.bulk.mapper.PositionApplyMapper;
import com.liyao.bulk.mapper.PositionMapper;
import com.liyao.bulk.model.PlatformUser;
import com.liyao.bulk.model.Position;
import com.liyao.bulk.model.PositionApply;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private static final String POSITION_TYPE_INTERNAL = "INTERNAL";
    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final String APPLY_PENDING = "PENDING";
    private static final String APPLY_APPROVED = "APPROVED";
    private static final String APPLY_REJECTED = "REJECTED";
    private static final String APPLY_CANCELED = "CANCELED";
    private static final String OP_ADD = "ADD";
    private static final String OP_MODIFY = "MODIFY";
    private static final String OP_CANCEL = "CANCEL";

    private final PositionMapper positionMapper;
    private final PositionApplyMapper positionApplyMapper;
    private final PlatformUserMapper platformUserMapper;
    private final LoginUserCacheService loginUserCacheService;
    private final String reviewMode;

    public PositionService(PositionMapper positionMapper,
                           PositionApplyMapper positionApplyMapper,
                           PlatformUserMapper platformUserMapper,
                           LoginUserCacheService loginUserCacheService,
                           @Value("${bulk.review-mode:INPUT}") String reviewMode) {
        this.positionMapper = positionMapper;
        this.positionApplyMapper = positionApplyMapper;
        this.platformUserMapper = platformUserMapper;
        this.loginUserCacheService = loginUserCacheService;
        this.reviewMode = reviewMode;
    }

    public List<PositionSummary> queryInternalPositions(Long deptId, String name, String status) {
        List<Position> positions = positionMapper.selectByCondition(deptId, name, status, POSITION_TYPE_INTERNAL);
        return positions.stream().map(this::toSummary).toList();
    }

    public PositionDetailResponse getPositionDetail(Long id) {
        Position position = requirePosition(id);
        PositionDetailResponse response = new PositionDetailResponse();
        response.setId(position.getId());
        response.setDeptId(position.getDeptId());
        response.setDeptName(position.getDeptName());
        response.setPostName(position.getPostName());
        response.setType(position.getPostType());
        response.setPostStatus(position.getPostStatus());
        response.setRemark(position.getRemark());
        response.setCreatedOperName(position.getCreatedOperName());
        response.setCreatedAt(position.getCreatedAt());
        response.setUpdatedOperName(position.getUpdatedOperName());
        response.setUpdatedAt(position.getUpdatedAt());
        response.setReviewOperName(position.getReviewOperName());
        response.setReviewTime(position.getReviewTime());
        response.setOperScopes(Collections.emptyList());
        return response;
    }

    public List<UserSummary> queryPositionUsers(Long positionId) {
        List<PlatformUser> users = platformUserMapper.selectByPositionId(positionId);
        return users.stream().map(this::toUserSummary).toList();
    }

    @Transactional
    public void createPositionApply(PositionCreateRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        validateCreateRequest(request);
        Position existing = positionMapper.selectByNameInDept(request.getDeptId(), request.getPostName());
        if (existing != null) {
            throw new BusinessException("岗位名称已存在");
        }
        if (positionApplyMapper.countPendingByDeptAndName(request.getDeptId(), request.getPostName()) > 0) {
            throw new BusinessException("已存在待复核申请");
        }
        createApply(request.getDeptId(), request.getDeptName(),
                null, request.getPostName(), request.getRemark(), OP_ADD, applicant);
    }

    @Transactional
    public void modifyPositionApply(Long positionId, PositionModifyRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        validateModifyRequest(request);
        Position position = requirePosition(positionId);
        if (!STATUS_NORMAL.equals(position.getPostStatus())) {
            throw new BusinessException("当前岗位状态不允许修改");
        }
        if (positionApplyMapper.countPendingByPositionId(positionId) > 0) {
            throw new BusinessException("已存在待复核申请");
        }
        if (!hasPositionChanges(position, request)) {
            throw new BusinessException("未检测到变更内容");
        }
        createApply(request.getDeptId(), request.getDeptName(),
                positionId, position.getPostName(), request.getRemark(), OP_MODIFY, applicant);
    }

    @Transactional
    public void cancelPositionApply(Long positionId, PositionCancelRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        Position position = requirePosition(positionId);
        if (!STATUS_NORMAL.equals(position.getPostStatus())) {
            throw new BusinessException("当前岗位状态不允许注销");
        }
        if (positionApplyMapper.countPendingByPositionId(positionId) > 0) {
            throw new BusinessException("已存在待复核申请");
        }
        createApply(request.getDeptId(), request.getDeptName(),
                positionId, position.getPostName(), position.getRemark(), OP_CANCEL, applicant);
    }

    public List<PositionApplySummary> queryPositionApplies(PositionApplyQueryRequest request) {
        return ApplyQuerySupport.queryAppliesWithName(
                request,
                PositionApplyQueryRequest::getDeptId,
                PositionApplyQueryRequest::getStatusType,
                PositionApplyQueryRequest::getStartDate,
                PositionApplyQueryRequest::getEndDate,
                PositionApplyQueryRequest::getArrNo,
                PositionApplyQueryRequest::getPostName,
                PositionApplyQueryRequest::getOperType,
                this::resolveApplyStatuses,
                this::parseStartTime,
                this::parseEndTime,
                positionApplyMapper::selectByCondition);
    }

    @Transactional
    public void revokeApply(Long applyId, Long operatorId) {
        ApplyRevokeSupport.revokeApply(
                applyId,
                operatorId,
                this::requireApply,
                PositionApply::getPostStatus,
                PositionApply::getOperCode,
                APPLY_PENDING,
                APPLY_CANCELED,
                positionApplyMapper::updateStatus);
    }

    @Transactional
    public void reviewApply(Long applyId, PositionApplyReviewRequest request) {
        ApplyReviewSupport.reviewApply(
                applyId,
                request,
                this::requireApply,
                PositionApply::getPostStatus,
                PositionApply::getOperCode,
                APPLY_PENDING,
                APPLY_REJECTED,
                APPLY_APPROVED,
                this::isInputReviewMode,
                PositionApply::getOperType,
                operationType -> !OP_CANCEL.equals(operationType),
                (apply, req) -> ensureReviewDataMatchesApply(apply, req),
                this::applyApproval,
                positionApplyMapper::updateStatus,
                PositionApplyReviewRequest::isApproved,
                PositionApplyReviewRequest::getReviewOperCode,
                PositionApplyReviewRequest::getReviewOperName,
                PositionApplyReviewRequest::getReviewRemark);
    }

    public List<PositionExportRow> buildPositionExport(Long deptId, String name, String status) {
        return queryInternalPositions(deptId, name, status).stream().map(item -> {
            PositionExportRow row = new PositionExportRow();
            row.setId(item.getId());
            row.setDeptName(item.getDeptName());
            row.setPostName(item.getPostName());
            row.setType(item.getType());
            row.setPostStatus(item.getPostStatus());
            row.setRemark(item.getRemark());
            row.setCreatedOperName(item.getCreatedOperName());
            row.setCreatedAt(item.getCreatedAt());
            row.setUpdatedOperName(item.getUpdatedOperName());
            row.setUpdatedAt(item.getUpdatedAt());
            row.setReviewOperName(item.getReviewOperName());
            row.setReviewTime(item.getReviewTime());
            return row;
        }).toList();
    }

    public List<PositionApplyExportRow> buildApplyExport(PositionApplyQueryRequest request) {
        return queryPositionApplies(request).stream().map(item -> {
            PositionApplyExportRow row = new PositionApplyExportRow();
            row.setArrNo(item.getArrNo());
            row.setPostName(item.getPostName());
            row.setOperType(item.getOperType());
            row.setPostStatus(item.getPostStatus());
            row.setArrOperName(item.getArrOperName());
            row.setArrDate(item.getArrDate());
            row.setReviewOperName(item.getReviewOperName());
            row.setReviewOperCode(item.getReviewOperCode());
            row.setReviewTime(item.getReviewTime());
            return row;
        }).toList();
    }

    private void applyApproval(PositionApply apply, PositionApplyReviewRequest request) {
        if (OP_ADD.equals(apply.getOperType())) {
            Position position = new Position();
            position.setDeptId(apply.getDeptId());
            position.setDeptName(apply.getDeptName());
            position.setPostName(apply.getPostName());
            position.setPostType(apply.getPostType());
            position.setRemark(apply.getRemark());
            position.setPostStatus(STATUS_NORMAL);
            position.setCreatedOperName(request.getReviewOperName());
            position.setCreatedAt(LocalDateTime.now());
            position.setUpdatedOperName(request.getReviewOperName());
            position.setUpdatedAt(LocalDateTime.now());
            position.setReviewOperName(request.getReviewOperName());
            position.setReviewTime(LocalDateTime.now());
            positionMapper.insert(position);
            return;
        }
        if (OP_MODIFY.equals(apply.getOperType())) {
            Position position = requirePosition(apply.getPostId());
            position.setRemark(apply.getRemark());
            position.setUpdatedOperName(request.getReviewOperName());
            position.setUpdatedAt(LocalDateTime.now());
            position.setReviewOperName(request.getReviewOperName());
            position.setReviewTime(LocalDateTime.now());
            positionMapper.update(position);
            return;
        }
        if (OP_CANCEL.equals(apply.getOperType())) {
            Position position = requirePosition(apply.getPostId());
            position.setPostStatus(STATUS_CANCELED);
            position.setUpdatedOperName(request.getReviewOperName());
            position.setUpdatedAt(LocalDateTime.now());
            position.setReviewOperName(request.getReviewOperName());
            position.setReviewTime(LocalDateTime.now());
            positionMapper.update(position);
        }
    }

    private void ensureReviewDataMatchesApply(PositionApply apply, PositionApplyReviewRequest request) {
        if (!Objects.equals(apply.getRemark(), request.getRemark())) {
            throw new BusinessException("复核数据与申请数据不一致");
        }
    }

    private PositionApply buildApply(Long deptId, String deptName, Long positionId, String name,
                                     String remark, String operationType, CurrentLoginUser applicant) {
        PositionApply apply = new PositionApply();
        apply.setArrNo(generateApplyNo());
        apply.setPostId(positionId);
        apply.setDeptId(deptId);
        apply.setDeptName(deptName);
        apply.setPostName(name);
        apply.setRemark(remark);
        apply.setPostType(POSITION_TYPE_INTERNAL);
        apply.setOperType(operationType);
        apply.setPostStatus(APPLY_PENDING);
        ApplyApplicantSupport.fillApplicantInfo(apply, applicant);
        return apply;
    }

    private PositionApply createApply(Long deptId, String deptName, Long positionId, String name,
                                      String remark, String operationType, CurrentLoginUser applicant) {
        PositionApply apply = buildApply(deptId, deptName, positionId, name, remark, operationType, applicant);
        positionApplyMapper.insert(apply);
        return apply;
    }

    private Position requirePosition(Long positionId) {
        Position position = positionMapper.selectById(positionId);
        if (position == null) {
            throw new BusinessException("岗位不存在");
        }
        return position;
    }

    private PositionApply requireApply(Long applyId) {
        PositionApply apply = positionApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        return apply;
    }

    private boolean hasPositionChanges(Position position, PositionModifyRequest request) {
        return !Objects.equals(position.getRemark(), request.getRemark());
    }

    private PositionSummary toSummary(Position position) {
        PositionSummary summary = new PositionSummary();
        summary.setId(position.getId());
        summary.setDeptId(position.getDeptId());
        summary.setDeptName(position.getDeptName());
        summary.setPostName(position.getPostName());
        summary.setType(position.getPostType());
        summary.setPostStatus(position.getPostStatus());
        summary.setRemark(position.getRemark());
        summary.setCreatedOperName(position.getCreatedOperName());
        summary.setCreatedAt(position.getCreatedAt());
        summary.setUpdatedOperName(position.getUpdatedOperName());
        summary.setUpdatedAt(position.getUpdatedAt());
        summary.setReviewOperName(position.getReviewOperName());
        summary.setReviewTime(position.getReviewTime());
        return summary;
    }

    private UserSummary toUserSummary(PlatformUser user) {
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setOperCode(user.getOperCode());
        summary.setOperName(user.getOperName());
        summary.setOperStatus(user.getOperStatus());
        summary.setPhone(user.getPhone());
        summary.setUserType(user.getUserType());
        return summary;
    }

    private void validateCreateRequest(PositionCreateRequest request) {
        if (request.getPostName() == null || request.getPostName().trim().isEmpty()) {
            throw new BusinessException("岗位名称不能为空");
        }
    }

    private void validateModifyRequest(PositionModifyRequest request) {
    }

    private List<String> resolveApplyStatuses(String statusType) {
        return ApplyStatusSupport.resolve(
                statusType,
                APPLY_APPROVED,
                APPLY_PENDING,
                APPLY_REJECTED,
                APPLY_CANCELED);
    }

    private LocalDateTime parseStartTime(String startDate) {
        if (startDate == null || startDate.isBlank()) {
            return null;
        }
        LocalDate date = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        return date.atStartOfDay();
    }

    private LocalDateTime parseEndTime(String endDate) {
        if (endDate == null || endDate.isBlank()) {
            return null;
        }
        LocalDate date = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        return LocalDateTime.of(date, LocalTime.MAX);
    }

    private boolean isInputReviewMode() {
        return "INPUT".equalsIgnoreCase(reviewMode);
    }

    private String generateApplyNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return timestamp + random;
    }
}

