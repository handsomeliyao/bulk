package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.dto.OperatorActionRequest;
import com.liyao.bulk.dto.OperatorApplyExportRow;
import com.liyao.bulk.dto.OperatorApplyQueryRequest;
import com.liyao.bulk.dto.OperatorApplyReviewRequest;
import com.liyao.bulk.dto.OperatorApplySummary;
import com.liyao.bulk.dto.OperatorAssignPermissionRequest;
import com.liyao.bulk.dto.OperatorCreateRequest;
import com.liyao.bulk.dto.OperatorDetailResponse;
import com.liyao.bulk.dto.OperatorExportRow;
import com.liyao.bulk.dto.OperatorModifyRequest;
import com.liyao.bulk.dto.OperatorPermissionItem;
import com.liyao.bulk.dto.OperatorSummary;
import com.liyao.bulk.dto.PositionOption;
import com.liyao.bulk.mapper.DepartmentMapper;
import com.liyao.bulk.mapper.OperatorApplyMapper;
import com.liyao.bulk.mapper.PlatformUserMapper;
import com.liyao.bulk.mapper.PositionMapper;
import com.liyao.bulk.mapper.PositionUserMapper;
import com.liyao.bulk.model.Department;
import com.liyao.bulk.model.OperatorApply;
import com.liyao.bulk.model.PlatformUser;
import com.liyao.bulk.model.Position;
import com.liyao.bulk.model.PositionUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperatorService {

    private static final String USER_TYPE_DEPT_OPERATOR = "DEPT_OPERATOR";
    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_RESET = "RESET";
    private static final String STATUS_FROZEN = "FROZEN";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final String APPLY_PENDING = "PENDING";
    private static final String APPLY_APPROVED = "APPROVED";
    private static final String APPLY_REJECTED = "REJECTED";
    private static final String APPLY_CANCELED = "CANCELED";
    private static final String OP_ADD = "ADD";
    private static final String OP_MODIFY = "MODIFY";
    private static final String OP_FREEZE = "FREEZE";
    private static final String OP_UNFREEZE = "UNFREEZE";
    private static final String OP_RESET_PWD = "RESET_PASSWORD";
    private static final String OP_CANCEL = "CANCEL";
    private static final String OP_ASSIGN_PERM = "ASSIGN_PERMISSION";
    private static final String POSITION_TYPE_INTERNAL = "INTERNAL";

    private final PlatformUserMapper platformUserMapper;
    private final DepartmentMapper departmentMapper;
    private final OperatorApplyMapper operatorApplyMapper;
    private final PositionMapper positionMapper;
    private final PositionUserMapper positionUserMapper;
    private final LoginUserCacheService loginUserCacheService;
    private final String reviewMode;

    public OperatorService(PlatformUserMapper platformUserMapper,
                           DepartmentMapper departmentMapper,
                           OperatorApplyMapper operatorApplyMapper,
                           PositionMapper positionMapper,
                           PositionUserMapper positionUserMapper,
                           LoginUserCacheService loginUserCacheService,
                           @Value("${bulk.review-mode:INPUT}") String reviewMode) {
        this.platformUserMapper = platformUserMapper;
        this.departmentMapper = departmentMapper;
        this.operatorApplyMapper = operatorApplyMapper;
        this.positionMapper = positionMapper;
        this.positionUserMapper = positionUserMapper;
        this.loginUserCacheService = loginUserCacheService;
        this.reviewMode = reviewMode;
    }

    public List<OperatorSummary> queryOperators(Long deptId, String operCode, String operName, String status) {
        List<PlatformUser> users = platformUserMapper.selectOperatorsByCondition(
                deptId, operCode, operName, status, USER_TYPE_DEPT_OPERATOR);
        return users.stream().map(this::toSummary).toList();
    }

    public OperatorDetailResponse getOperatorDetail(Long userId, Long deptId) {
        PlatformUser user = requireOperator(userId, deptId);
        List<Position> positions = positionMapper.selectByDeptAndTypeStatus(deptId, POSITION_TYPE_INTERNAL, STATUS_NORMAL);
        List<Position> selectedPositions = positionMapper.selectByUserId(userId);
        Set<Long> selectedIds = selectedPositions.stream().map(Position::getId).collect(Collectors.toSet());
        List<PositionOption> options = positions.stream().map(position -> {
            PositionOption option = new PositionOption();
            option.setPostId(position.getId());
            option.setPostName(position.getPostName());
            option.setSelected(selectedIds.contains(position.getId()));
            return option;
        }).toList();
        List<OperatorPermissionItem> permissions = Collections.emptyList();

        OperatorDetailResponse response = new OperatorDetailResponse();
        UserDetailAssembler.fillBase(response, user, deptId, fetchDeptName(deptId));
        response.setPositions(options);
        response.setPermissions(permissions);
        return response;
    }

    public List<OperatorPermissionItem> refreshPermissions(Long userId, Long deptId) {
        requireOperator(userId, deptId);
        return Collections.emptyList();
    }

    @Transactional
    public void createOperatorApply(OperatorCreateRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        validateCreateRequest(request);
        ApplyValidationSupport.ensureOperCodeAvailable(
                platformUserMapper,
                operatorApplyMapper::countPendingByOperCode,
                request.getOperCode());
        OperatorApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), 0L,
                request.getOperCode(), OP_ADD, request.getRemark(), request);
        operatorApplyMapper.insert(apply);
    }

    @Transactional
    public void modifyOperatorApply(Long userId, OperatorModifyRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireOperator(userId, resolveDeptId(request.getDeptId(), applicant));
        if (request.getOperName() == null || request.getOperName().trim().isEmpty()) {
            throw new BusinessException("Full name is required");
        }
        if (!STATUS_NORMAL.equals(user.getOperStatus()) && !STATUS_RESET.equals(user.getOperStatus())) {
            throw new BusinessException("Status does not allow modification");
        }
        if (operatorApplyMapper.countPendingByUserId(userId) > 0) {
            throw new BusinessException("Pending applications exist");
        }
        if (!hasOperatorChanges(user, request)) {
            throw new BusinessException("No changes to apply");
        }
        OperatorApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_MODIFY, request.getRemark(), request);
        operatorApplyMapper.insert(apply);
    }

    @Transactional
    public void freezeOperator(Long userId, OperatorActionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireOperator(userId, resolveDeptId(request.getDeptId(), applicant));
        validatePendingAndSelf(user, applicant, STATUS_NORMAL, "Status does not allow freeze");
        OperatorApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_FREEZE, null, request);
        operatorApplyMapper.insert(apply);
    }

    @Transactional
    public void unfreezeOperator(Long userId, OperatorActionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireOperator(userId, resolveDeptId(request.getDeptId(), applicant));
        validatePendingAndSelf(user, applicant, STATUS_FROZEN, "Status does not allow unfreeze");
        OperatorApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_UNFREEZE, null, request);
        operatorApplyMapper.insert(apply);
    }

    @Transactional
    public void resetPassword(Long userId, OperatorActionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireOperator(userId, resolveDeptId(request.getDeptId(), applicant));
        validatePendingAndSelf(user, applicant, STATUS_NORMAL, "Status does not allow reset password");
        OperatorApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_RESET_PWD, null, request);
        operatorApplyMapper.insert(apply);
    }

    @Transactional
    public void cancelOperator(Long userId, OperatorActionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireOperator(userId, resolveDeptId(request.getDeptId(), applicant));
        if (!STATUS_NORMAL.equals(user.getOperStatus()) && !STATUS_RESET.equals(user.getOperStatus())) {
            throw new BusinessException("Status does not allow cancel");
        }
        validatePendingAndSelf(user, applicant, null, null);
        OperatorApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_CANCEL, null, request);
        operatorApplyMapper.insert(apply);
    }

    @Transactional
    public void assignPermissions(Long userId, OperatorAssignPermissionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireOperator(userId, resolveDeptId(request.getDeptId(), applicant));
        validateStatusAndPendingForNormalOrReset(user);
        ensureSelectionChanged(currentPositionIds(userId), request.getPostIds(), "Positions are required");
        OperatorApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_ASSIGN_PERM, null, request);
        operatorApplyMapper.insert(apply);
    }

    public List<OperatorApplySummary> queryOperatorApplies(OperatorApplyQueryRequest request) {
        return ApplyQuerySupport.queryApplies(
                request,
                this::resolveApplyStatuses,
                this::parseStartTime,
                this::parseEndTime,
                operatorApplyMapper::selectByCondition);
    }

    @Transactional
    public void revokeApply(Long applyId, Long operatorId) {
        ApplyRevokeSupport.revokeApply(
                applyId,
                operatorId,
                this::requireApply,
                OperatorApply::getOperStatus,
                OperatorApply::getOperCode,
                APPLY_PENDING,
                APPLY_CANCELED,
                operatorApplyMapper::updateStatus);
    }

    @Transactional
    public void reviewApply(Long applyId, OperatorApplyReviewRequest request) {
        ApplyReviewSupport.reviewApply(
                applyId,
                request,
                this::requireApply,
                OperatorApply::getOperStatus,
                OperatorApply::getOperCode,
                APPLY_PENDING,
                APPLY_REJECTED,
                APPLY_APPROVED,
                this::isInputReviewMode,
                OperatorApply::getOperType,
                this::requiresInputReview,
                this::ensureReviewDataMatchesApply,
                this::applyApproval,
                operatorApplyMapper::updateStatus,
                OperatorApplyReviewRequest::isApproved,
                OperatorApplyReviewRequest::getReviewOperCode,
                OperatorApplyReviewRequest::getReviewOperName,
                OperatorApplyReviewRequest::getReviewRemark);
    }

    public List<OperatorExportRow> buildOperatorExport(Long deptId, String operCode, String operName, String status) {
        return queryOperators(deptId, operCode, operName, status).stream().map(item -> {
            OperatorExportRow row = new OperatorExportRow();
            row.setId(item.getId());
            row.setOperCode(item.getOperCode());
            row.setOperName(item.getOperName());
            row.setUserType(item.getUserType());
            row.setOperStatus(item.getOperStatus());
            row.setTelPhone(item.getTelPhone());
            row.setPhone(item.getPhone());
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

    public List<OperatorApplyExportRow> buildApplyExport(OperatorApplyQueryRequest request) {
        return queryOperatorApplies(request).stream().map(item -> {
            OperatorApplyExportRow row = new OperatorApplyExportRow();
            row.setArrNo(item.getArrNo());
            row.setArrOperCode(item.getArrOperCode());
            row.setOperType(item.getOperType());
            row.setOperStatus(item.getOperStatus());
            row.setArrOperName(item.getArrOperName());
            row.setArrDate(item.getArrDate());
            row.setReviewOperName(item.getReviewOperName());
            row.setReviewOperCode(item.getReviewOperCode());
            row.setReviewTime(item.getReviewTime());
            return row;
        }).toList();
    }

    private PlatformUser requireOperator(Long userId, Long deptId) {
        PlatformUser user = platformUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("Operator not found");
        }
        if (deptId != null && !Objects.equals(deptId, user.getDeptId())) {
            throw new BusinessException("No permission for this operator");
        }
        if (!USER_TYPE_DEPT_OPERATOR.equals(user.getUserType())) {
            throw new BusinessException("User type is not operator");
        }
        return user;
    }

    private void validateCreateRequest(OperatorCreateRequest request) {
        UserCreateValidationSupport.validate(
                request.getDeptId(),
                request.getOperCode(),
                request.getOperName());
    }

    private boolean hasOperatorChanges(PlatformUser user, OperatorModifyRequest request) {
        return !Objects.equals(user.getOperName(), request.getOperName())
                || !Objects.equals(user.getTelPhone(), request.getTelPhone())
                || !Objects.equals(user.getPhone(), request.getPhone())
                || !Objects.equals(user.getRemark(), request.getRemark());
    }

    private void validatePendingAndSelf(PlatformUser user, CurrentLoginUser applicant,
                                        String requiredStatus, String statusMessage) {
        if (requiredStatus != null && !requiredStatus.equals(user.getOperStatus())) {
            throw new BusinessException(statusMessage);
        }
        if (operatorApplyMapper.countPendingByUserId(user.getId()) > 0) {
            throw new BusinessException("Pending applications exist");
        }
        if (Objects.equals(user.getId(), applicant.getOperCode())) {
            throw new BusinessException("Applicant cannot be the target user");
        }
    }

    private void validateStatusAndPendingForNormalOrReset(PlatformUser user) {
        if (!STATUS_NORMAL.equals(user.getOperStatus()) && !STATUS_RESET.equals(user.getOperStatus())) {
            throw new BusinessException("Invalid status");
        }
        if (operatorApplyMapper.countPendingByUserId(user.getId()) > 0) {
            throw new BusinessException("Pending applications exist");
        }
    }

    private OperatorApply buildApply(Long deptId, String deptName, Long userId,
                                     String operCode, String operationType, String remark,
                                     Object applicantSource) {
        OperatorApply apply = new OperatorApply();
        apply.setArrNo(generateApplyNo());
        apply.setDeptId(deptId);
        apply.setDeptName(deptName);
        fillContact(apply, applicantSource);
        apply.setRemark(remark);
        apply.setOperType(operationType);
        apply.setOperStatus(APPLY_PENDING);
        apply.setOperCode(userId);
        apply.setOperName(operCode);
        apply.setArrDate(LocalDateTime.now());
        return apply;
    }

    private OperatorApply requireApply(Long applyId) {
        OperatorApply apply = operatorApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("Apply record not found");
        }
        return apply;
    }

    private void applyApproval(OperatorApply apply, OperatorApplyReviewRequest request) {
        if (OP_ADD.equals(apply.getOperType())) {
            PlatformUser user = new PlatformUser();
            user.setOperCode(request.getOperCode());
            user.setOperName(request.getOperName());
            user.setOperStatus(STATUS_RESET);
            user.setUserType(USER_TYPE_DEPT_OPERATOR);
            user.setTelPhone(request.getTelPhone());
            user.setPhone(request.getPhone());
            user.setRemark(request.getRemark());
            user.setDeptId(apply.getDeptId());
            user.setCreatedOperName(request.getReviewOperName());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedOperName(request.getReviewOperName());
            user.setUpdatedAt(LocalDateTime.now());
            user.setReviewOperName(request.getReviewOperName());
            user.setReviewTime(LocalDateTime.now());
            platformUserMapper.insert(user);
            return;
        }
        if (OP_MODIFY.equals(apply.getOperType())) {
            PlatformUser user = platformUserMapper.selectById(apply.getOperCode());
            if (user != null) {
                user.setOperName(request.getOperName());
                user.setTelPhone(request.getTelPhone());
                user.setPhone(request.getPhone());
                user.setRemark(request.getRemark());
                user.setUpdatedOperName(request.getReviewOperName());
                user.setUpdatedAt(LocalDateTime.now());
                user.setReviewOperName(request.getReviewOperName());
                user.setReviewTime(LocalDateTime.now());
                platformUserMapper.update(user);
            }
            return;
        }
        if (OP_FREEZE.equals(apply.getOperType())) {
            platformUserMapper.updateStatus(apply.getOperCode(), STATUS_FROZEN);
            return;
        }
        if (OP_UNFREEZE.equals(apply.getOperType())) {
            platformUserMapper.updateStatus(apply.getOperCode(), STATUS_RESET);
            return;
        }
        if (OP_RESET_PWD.equals(apply.getOperType())) {
            platformUserMapper.updateStatus(apply.getOperCode(), STATUS_RESET);
            return;
        }
        if (OP_CANCEL.equals(apply.getOperType())) {
            platformUserMapper.updateStatus(apply.getOperCode(), STATUS_CANCELED);
            return;
        }
        if (OP_ASSIGN_PERM.equals(apply.getOperType())) {
            positionUserMapper.deleteByOperCode(apply.getOperCode());
            List<Long> positionIds = request.getPostIds();
            if (positionIds == null || positionIds.isEmpty()) {
                return;
            }
            List<PositionUser> items = positionIds.stream().map(positionId -> {
                PositionUser item = new PositionUser();
                item.setOperCode(apply.getOperCode());
                item.setPostId(positionId);
                item.setCreatedAt(LocalDateTime.now());
                return item;
            }).toList();
            if (!items.isEmpty()) {
                positionUserMapper.insertBatch(items);
            }
        }
    }

    private void ensureReviewDataMatchesApply(OperatorApply apply, OperatorApplyReviewRequest request) {
    }

    private boolean requiresInputReview(String operationType) {
        return OP_ADD.equals(operationType)
                || OP_MODIFY.equals(operationType)
                || OP_ASSIGN_PERM.equals(operationType);
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
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return timestamp + random;
    }

    private String fetchDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        Department department = departmentMapper.selectById(deptId);
        return department == null ? null : department.getDeptName();
    }

    private void fillContact(OperatorApply apply, Object source) {
        if (source instanceof OperatorCreateRequest req) {
            apply.setTelPhone(req.getTelPhone());
            apply.setMobile(req.getPhone());
            return;
        }
        if (source instanceof OperatorModifyRequest req) {
            apply.setTelPhone(req.getTelPhone());
            apply.setMobile(req.getPhone());
            return;
        }
        apply.setTelPhone(null);
        apply.setMobile(null);
    }

    private OperatorSummary toSummary(PlatformUser user) {
        OperatorSummary summary = new OperatorSummary();
        summary.setId(user.getId());
        summary.setOperCode(user.getOperCode());
        summary.setOperName(user.getOperName());
        summary.setUserType(user.getUserType());
        summary.setOperStatus(user.getOperStatus());
        summary.setTelPhone(user.getTelPhone());
        summary.setPhone(user.getPhone());
        summary.setRemark(user.getRemark());
        summary.setCreatedOperName(user.getCreatedOperName());
        summary.setCreatedAt(user.getCreatedAt());
        summary.setUpdatedOperName(user.getUpdatedOperName());
        summary.setUpdatedAt(user.getUpdatedAt());
        summary.setReviewOperName(user.getReviewOperName());
        summary.setReviewTime(user.getReviewTime());
        return summary;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private Set<Long> currentPositionIds(Long userId) {
        return positionUserMapper.selectByOperCode(userId).stream()
                .map(PositionUser::getPostId)
                .collect(Collectors.toSet());
    }

    private void ensureSelectionChanged(Set<Long> current, List<Long> incoming, String emptyMessage) {
        if (incoming == null || incoming.isEmpty()) {
            throw new BusinessException(emptyMessage);
        }
        Set<Long> incomingSet = new HashSet<>(incoming);
        if (current.equals(incomingSet)) {
            throw new BusinessException("No changes to apply");
        }
    }

    private Long resolveDeptId(Long requestDeptId, CurrentLoginUser applicant) {
        return requestDeptId != null ? requestDeptId : applicant.getApplicantDeptId();
    }

    private String resolveDeptName(String requestDeptName, CurrentLoginUser applicant) {
        return requestDeptName != null ? requestDeptName : applicant.getApplicantDeptName();
    }
}

