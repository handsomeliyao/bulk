package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.dto.AdminActionRequest;
import com.liyao.bulk.dto.AdminApplyExportRow;
import com.liyao.bulk.dto.AdminApplyQueryRequest;
import com.liyao.bulk.dto.AdminApplyReviewRequest;
import com.liyao.bulk.dto.AdminApplySummary;
import com.liyao.bulk.dto.AdminCreateRequest;
import com.liyao.bulk.dto.AdminDetailResponse;
import com.liyao.bulk.dto.AdminExportRow;
import com.liyao.bulk.dto.AdminModifyRequest;
import com.liyao.bulk.dto.AdminPermissionResponse;
import com.liyao.bulk.dto.AdminSummary;
import com.liyao.bulk.mapper.AdminApplyMapper;
import com.liyao.bulk.mapper.DepartmentMapper;
import com.liyao.bulk.mapper.PlatformUserMapper;
import com.liyao.bulk.model.AdminApply;
import com.liyao.bulk.model.Department;
import com.liyao.bulk.model.PlatformUser;
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
public class AdminService {

    private static final String USER_TYPE_DEPT_ADMIN = "DEPT_ADMIN";
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

    private final PlatformUserMapper platformUserMapper;
    private final DepartmentMapper departmentMapper;
    private final AdminApplyMapper adminApplyMapper;
    private final LoginUserCacheService loginUserCacheService;
    private final String reviewMode;

    public AdminService(PlatformUserMapper platformUserMapper,
                        DepartmentMapper departmentMapper,
                        AdminApplyMapper adminApplyMapper,
                        LoginUserCacheService loginUserCacheService,
                        @Value("${bulk.review-mode:INPUT}") String reviewMode) {
        this.platformUserMapper = platformUserMapper;
        this.departmentMapper = departmentMapper;
        this.adminApplyMapper = adminApplyMapper;
        this.loginUserCacheService = loginUserCacheService;
        this.reviewMode = reviewMode;
    }

    public List<AdminSummary> queryAdmins(Long deptId, String operCode, String operName, String status) {
        List<PlatformUser> users = platformUserMapper.selectOperatorsByCondition(
                deptId, operCode, operName, status, USER_TYPE_DEPT_ADMIN);
        return users.stream().map(this::toSummary).toList();
    }

    public AdminDetailResponse getAdminDetail(Long userId, Long deptId) {
        PlatformUser user = requireAdmin(userId, deptId);
        Long resolvedDeptId = resolveDeptId(userId, deptId);
        AdminPermissionResponse permissions = buildPermissions(resolvedDeptId);
        AdminDetailResponse response = new AdminDetailResponse();
        UserDetailAssembler.fillBase(response, user, resolvedDeptId, fetchDeptName(resolvedDeptId));
        response.setAuthScopes(permissions.getAuthScopes());
        response.setOperScopes(permissions.getOperScopes());
        return response;
    }

    public AdminPermissionResponse refreshPermissions(Long userId, Long deptId) {
        requireAdmin(userId, deptId);
        Long resolvedDeptId = resolveDeptId(userId, deptId);
        return buildPermissions(resolvedDeptId);
    }

    @Transactional
    public void createAdminApply(AdminCreateRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        validateCreateRequest(request);
        ApplyValidationSupport.ensureOperCodeAvailable(
                platformUserMapper,
                adminApplyMapper::countPendingByOperCode,
                request.getOperCode());
        AdminApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), 0L,
                request.getOperCode(), OP_ADD, request.getRemark(), request);
        adminApplyMapper.insert(apply);
    }

    @Transactional
    public void modifyAdminApply(Long userId, AdminModifyRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireAdmin(userId, resolveDeptId(request.getDeptId(), applicant));
        if (request.getOperName() == null || request.getOperName().trim().isEmpty()) {
            throw new BusinessException("Full name is required");
        }
        if (!STATUS_NORMAL.equals(user.getOperStatus()) && !STATUS_RESET.equals(user.getOperStatus())) {
            throw new BusinessException("Status does not allow modification");
        }
        if (adminApplyMapper.countPendingByUserId(userId) > 0) {
            throw new BusinessException("Pending applications exist");
        }
        if (!hasAdminChanges(user, request)) {
            throw new BusinessException("No changes to apply");
        }
        AdminApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_MODIFY, request.getRemark(), request);
        adminApplyMapper.insert(apply);
    }

    @Transactional
    public void freezeAdmin(Long userId, AdminActionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireAdmin(userId, resolveDeptId(request.getDeptId(), applicant));
        validatePendingAndSelf(user, applicant, STATUS_NORMAL, "Status does not allow freeze");
        AdminApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_FREEZE, null, request);
        adminApplyMapper.insert(apply);
    }

    @Transactional
    public void unfreezeAdmin(Long userId, AdminActionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireAdmin(userId, resolveDeptId(request.getDeptId(), applicant));
        validatePendingAndSelf(user, applicant, STATUS_FROZEN, "Status does not allow unfreeze");
        AdminApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_UNFREEZE, null, request);
        adminApplyMapper.insert(apply);
    }

    @Transactional
    public void resetPassword(Long userId, AdminActionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireAdmin(userId, resolveDeptId(request.getDeptId(), applicant));
        validatePendingAndSelf(user, applicant, STATUS_NORMAL, "Status does not allow reset password");
        AdminApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_RESET_PWD, null, request);
        adminApplyMapper.insert(apply);
    }

    @Transactional
    public void cancelAdmin(Long userId, AdminActionRequest request) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        PlatformUser user = requireAdmin(userId, resolveDeptId(request.getDeptId(), applicant));
        if (!STATUS_NORMAL.equals(user.getOperStatus()) && !STATUS_RESET.equals(user.getOperStatus())) {
            throw new BusinessException("Status does not allow cancel");
        }
        validatePendingAndSelf(user, applicant, null, null);
        AdminApply apply = buildApply(resolveDeptId(request.getDeptId(), applicant),
                resolveDeptName(request.getDeptName(), applicant), userId,
                user.getOperCode(), OP_CANCEL, null, request);
        adminApplyMapper.insert(apply);
    }

    public List<AdminApplySummary> queryAdminApplies(AdminApplyQueryRequest request) {
        return ApplyQuerySupport.queryApplies(
                request,
                this::resolveApplyStatuses,
                this::parseStartTime,
                this::parseEndTime,
                adminApplyMapper::selectByCondition);
    }

    @Transactional
    public void revokeApply(Long applyId, Long operatorId) {
        ApplyRevokeSupport.revokeApply(
                applyId,
                operatorId,
                this::requireApply,
                AdminApply::getOperStatus,
                AdminApply::getOperCode,
                APPLY_PENDING,
                APPLY_CANCELED,
                adminApplyMapper::updateStatus);
    }

    @Transactional
    public void reviewApply(Long applyId, AdminApplyReviewRequest request) {
        ApplyReviewSupport.reviewApply(
                applyId,
                request,
                this::requireApply,
                AdminApply::getOperStatus,
                AdminApply::getOperCode,
                APPLY_PENDING,
                APPLY_REJECTED,
                APPLY_APPROVED,
                this::isInputReviewMode,
                AdminApply::getOperationType,
                this::requiresInputReview,
                this::ensureReviewDataMatchesApply,
                this::applyApproval,
                adminApplyMapper::updateStatus,
                AdminApplyReviewRequest::isApproved,
                AdminApplyReviewRequest::getReviewOperCode,
                AdminApplyReviewRequest::getReviewOperName,
                AdminApplyReviewRequest::getReviewRemark);
    }

    public List<AdminExportRow> buildAdminExport(Long deptId, String operCode, String operName, String status) {
        return queryAdmins(deptId, operCode, operName, status).stream().map(item -> {
            AdminExportRow row = new AdminExportRow();
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

    public List<AdminApplyExportRow> buildApplyExport(AdminApplyQueryRequest request) {
        return queryAdminApplies(request).stream().map(item -> {
            AdminApplyExportRow row = new AdminApplyExportRow();
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

    private PlatformUser requireAdmin(Long userId, Long deptId) {
        PlatformUser user = platformUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("Admin not found");
        }
        if (deptId != null && !Objects.equals(deptId, user.getDeptId())) {
            throw new BusinessException("No permission for this admin");
        }
        if (!USER_TYPE_DEPT_ADMIN.equals(user.getUserType())) {
            throw new BusinessException("User type is not admin");
        }
        return user;
    }

    private void validateCreateRequest(AdminCreateRequest request) {
        UserCreateValidationSupport.validate(
                request.getDeptId(),
                request.getOperCode(),
                request.getOperName());
    }

    private boolean hasAdminChanges(PlatformUser user, AdminModifyRequest request) {
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
        if (adminApplyMapper.countPendingByUserId(user.getId()) > 0) {
            throw new BusinessException("Pending applications exist");
        }
        if (Objects.equals(user.getId(), applicant.getOperCode())) {
            throw new BusinessException("Applicant cannot be the target user");
        }
    }

    private AdminApply buildApply(Long deptId, String deptName, Long userId,
                                  String operCode, String operationType, String remark,
                                  Object applicantSource) {
        AdminApply apply = new AdminApply();
        apply.setArrNo(generateApplyNo());
        apply.setDeptId(deptId);
        fillContact(apply, applicantSource);
        apply.setRemark(remark);
        apply.setOperType(USER_TYPE_DEPT_ADMIN);
        apply.setOperationType(operationType);
        apply.setOperStatus(APPLY_PENDING);
        apply.setOperCode(userId);
        apply.setOperName(operCode);
        apply.setArrDate(LocalDateTime.now());
        return apply;
    }

    private AdminApply requireApply(Long applyId) {
        AdminApply apply = adminApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("Apply record not found");
        }
        return apply;
    }

    private void applyApproval(AdminApply apply, AdminApplyReviewRequest request) {
        if (OP_ADD.equals(apply.getOperationType())) {
            PlatformUser user = new PlatformUser();
            user.setOperCode(request.getOperCode());
            user.setOperName(request.getOperName());
            user.setOperStatus(STATUS_RESET);
            user.setUserType(USER_TYPE_DEPT_ADMIN);
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
        if (OP_MODIFY.equals(apply.getOperationType())) {
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
        if (OP_FREEZE.equals(apply.getOperationType())) {
            platformUserMapper.updateStatus(apply.getOperCode(), STATUS_FROZEN);
            return;
        }
        if (OP_UNFREEZE.equals(apply.getOperationType())) {
            platformUserMapper.updateStatus(apply.getOperCode(), STATUS_RESET);
            return;
        }
        if (OP_RESET_PWD.equals(apply.getOperationType())) {
            platformUserMapper.updateStatus(apply.getOperCode(), STATUS_RESET);
            return;
        }
        if (OP_CANCEL.equals(apply.getOperationType())) {
            platformUserMapper.updateStatus(apply.getOperCode(), STATUS_CANCELED);
        }
    }

    private void ensureReviewDataMatchesApply(AdminApply apply, AdminApplyReviewRequest request) {
    }

    private boolean requiresInputReview(String operationType) {
        return OP_ADD.equals(operationType) || OP_MODIFY.equals(operationType);
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

    private void fillContact(AdminApply apply, Object source) {
        if (source instanceof AdminCreateRequest req) {
            apply.setTelPhone(req.getTelPhone());
            apply.setMobile(req.getPhone());
            return;
        }
        if (source instanceof AdminModifyRequest req) {
            apply.setTelPhone(req.getTelPhone());
            apply.setMobile(req.getPhone());
            return;
        }
        apply.setTelPhone(null);
        apply.setMobile(null);
    }

    private AdminSummary toSummary(PlatformUser user) {
        AdminSummary summary = new AdminSummary();
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

    private Long resolveDeptId(Long userId, Long deptId) {
        if (deptId != null) {
            return deptId;
        }
        PlatformUser user = platformUserMapper.selectById(userId);
        return user == null ? null : user.getDeptId();
    }

    private AdminPermissionResponse buildPermissions(Long deptId) {
        AdminPermissionResponse response = new AdminPermissionResponse();
        response.setAuthScopes(Collections.emptyList());
        response.setOperScopes(Collections.emptyList());
        return response;
    }

    private Long resolveDeptId(Long requestDeptId, CurrentLoginUser applicant) {
        return requestDeptId != null ? requestDeptId : applicant.getApplicantDeptId();
    }

    private String resolveDeptName(String requestDeptName, CurrentLoginUser applicant) {
        return requestDeptName != null ? requestDeptName : applicant.getApplicantDeptName();
    }
}

