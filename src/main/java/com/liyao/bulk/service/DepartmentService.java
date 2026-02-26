package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.dto.*;
import com.liyao.bulk.mapper.DepartmentApplyMapper;
import com.liyao.bulk.mapper.DepartmentButtonPermissionMapper;
import com.liyao.bulk.mapper.DepartmentMapper;
import com.liyao.bulk.mapper.PlatformUserMapper;
import com.liyao.bulk.model.DepartmentButtonPermission;
import com.liyao.bulk.model.Department;
import com.liyao.bulk.model.DepartmentApply;
import com.liyao.bulk.model.PlatformUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DepartmentService {

    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_CANCELED = "CANCELED";

    // 部门申请状态枚举：待复核、复核通过、复核拒绝、已撤销
    private static final String APPLY_PENDING = "PENDING";
    private static final String APPLY_APPROVED = "APPROVED";
    private static final String APPLY_REJECTED = "REJECTED";
    private static final String APPLY_CANCELED = "CANCELED";

    // 待复核分页可查询状态：待复核、复核通过、复核拒绝、已撤销
    private static final List<String> DEPARTMENT_PENDING_STATUSES = List.of(
            APPLY_PENDING,
            APPLY_REJECTED,
            APPLY_CANCELED
    );
    private static final List<String> DEPARTMENT_REVIEWED_STATUSES = List.of(
            APPLY_APPROVED
    );

    private static final String OP_ADD = "ADD";
    private static final String OP_MODIFY = "MODIFY";
    private static final String OP_CANCEL = "CANCEL";
    private static final String PERMISSION_TYPE_OPER = "1";
    private static final String PERMISSION_TYPE_ASSIGN = "2";

    private final DepartmentMapper departmentMapper;
    private final DepartmentApplyMapper departmentApplyMapper;
    private final DepartmentButtonPermissionMapper departmentButtonPermissionMapper;
    private final PlatformUserMapper platformUserMapper;
    private final LoginUserCacheService loginUserCacheService;

    public DepartmentService(DepartmentMapper departmentMapper,
                             DepartmentApplyMapper departmentApplyMapper,
                             DepartmentButtonPermissionMapper departmentButtonPermissionMapper,
                             PlatformUserMapper platformUserMapper,
                             LoginUserCacheService loginUserCacheService) {
        this.departmentMapper = departmentMapper;
        this.departmentApplyMapper = departmentApplyMapper;
        this.departmentButtonPermissionMapper = departmentButtonPermissionMapper;
        this.platformUserMapper = platformUserMapper;
        this.loginUserCacheService = loginUserCacheService;
    }

    public List<DepartmentSummary> queryDepartments(String name, String status) {
        List<Department> departments = departmentMapper.selectByCondition(name, status);
        return departments.stream().map(department -> {
            DepartmentSummary summary = toSummary(department);
            fillDepartmentAuth(summary, department.getId());
            return summary;
        }).toList();
    }

    public DepartmentDetailResponse getDepartmentDetail(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("Department not found");
        }
        DepartmentDetailResponse response = new DepartmentDetailResponse();
        response.setId(department.getId());
        response.setDeptName(department.getDeptName());
        response.setRemark(department.getRemark());
        response.setDeptStatus(department.getDeptStatus());
        response.setCreatedOperName(department.getCreatedOperName());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedOperName(department.getUpdatedOperName());
        response.setUpdatedAt(department.getUpdatedAt());
        response.setReviewOperName(department.getReviewOperName());
        response.setReviewTime(department.getReviewTime());
        response.setAuthScopes(Collections.emptyList());
        response.setOperScopes(Collections.emptyList());
        DepartmentAuth auth = resolveDepartmentAuth(department.getId());
        response.setAssignAuth(auth.assignAuth());
        response.setOperAuth(auth.operAuth());
        return response;
    }

    public List<UserSummary> queryDepartmentUsers(Long deptId) {
        List<PlatformUser> users = platformUserMapper.selectByDeptId(deptId);
        return users.stream().map(this::toUserSummary).toList();
    }

    @Transactional
    public void createDepartmentApply(DepartmentCreateRequest request) {
        validateDepartmentRequest(request.getDeptName());
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        Department existing = departmentMapper.selectByName(request.getDeptName());
        if (existing != null) {
            throw new BusinessException("Department name already exists");
        }
        DepartmentApply apply = buildApply(request.getDeptName(), request.getRemark(), applicant.getApplicantDeptId(),
                STATUS_NORMAL, OP_ADD, applicant);
        departmentApplyMapper.insert(apply);
        saveDeptButtonPermissions(apply.getDeptId(), request.getAssignAuth(), request.getOperAuth());
    }

    @Transactional
    public void modifyDepartmentApply(Long deptId, DepartmentModifyRequest request) {
        validateDepartmentRequest(request.getDeptName());
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        Department department = requireDepartment(deptId);
        if (!STATUS_NORMAL.equals(department.getDeptStatus())) {
            throw new BusinessException("Department status does not allow modification");
        }
        if (departmentApplyMapper.countPendingByDeptId(deptId) > 0) {
            throw new BusinessException("Pending applications exist");
        }
        Department existing = departmentMapper.selectByName(request.getDeptName());
        if (existing != null && !existing.getId().equals(deptId)) {
            throw new BusinessException("Department name already exists");
        }
        if (!hasDepartmentChanges(department, request)) {
            throw new BusinessException("No changes to apply");
        }
        DepartmentApply apply = buildApply(request.getDeptName(), request.getRemark(), deptId, department.getDeptStatus(), OP_MODIFY, applicant);
        departmentApplyMapper.insert(apply);
    }

    @Transactional
    public void cancelDepartmentApply(Long deptId) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        Department department = requireDepartment(deptId);
        if (!STATUS_NORMAL.equals(department.getDeptStatus())) {
            throw new BusinessException("Department status does not allow cancel");
        }
        if (departmentApplyMapper.countPendingByDeptId(deptId) > 0) {
            throw new BusinessException("Pending applications exist");
        }
        DepartmentApply apply = new DepartmentApply();
        apply.setArrNo(generateApplyNo());
        apply.setDeptId(deptId);
        apply.setDeptName(department.getDeptName());
        apply.setRemark(department.getRemark());
        apply.setOperType(OP_CANCEL);
        // 申请创建时默认状态：待复核
        apply.setOperStatus(APPLY_PENDING);
        apply.setDeptStatus(department.getDeptStatus());
        fillApplicantInfo(apply, applicant);
        departmentApplyMapper.insert(apply);
    }

    public List<DepartmentApplySummary> queryPendingDepartmentApplies(DepartmentApplyQueryRequest request) {
        return queryDepartmentAppliesByStatuses(request, DEPARTMENT_PENDING_STATUSES);
    }

    public List<DepartmentApplySummary> queryReviewedDepartmentApplies(DepartmentApplyQueryRequest request) {
        return queryDepartmentAppliesByStatuses(request, DEPARTMENT_REVIEWED_STATUSES);
    }

    @Transactional
    public void revokeApply(Long applyId) {
        CurrentLoginUser operator = loginUserCacheService.getRequiredCurrentUser();
        ApplyRevokeSupport.revokeApply(
                applyId,
                operator.getLoginOperCode(),
                this::requireApply,
                DepartmentApply::getOperStatus,
                DepartmentApply::getOperCode,
                APPLY_PENDING,
                APPLY_CANCELED,
                departmentApplyMapper::updateStatus);
    }

    @Transactional
    public void reviewApply(DepartmentApplyApproveRequest request) {
        if (request == null || request.getApplyId() == null) {
            throw new BusinessException("Apply id is required");
        }
        validateDepartmentRequest(request.getDeptName());
        CurrentLoginUser reviewer = loginUserCacheService.getRequiredCurrentUser();
        DepartmentApply apply = requireApply(request.getApplyId());
        if (!APPLY_PENDING.equals(apply.getOperStatus())) {
            throw new BusinessException("Only pending applies can be reviewed");
        }
        if (Objects.equals(apply.getOperCode(), reviewer.getLoginOperCode())) {
            throw new BusinessException("Cannot review your own apply");
        }
        if (!OP_ADD.equals(apply.getOperType())) {
            throw new BusinessException("Only ADD apply is supported for this review API");
        }
        if (!Objects.equals(apply.getDeptName(), request.getDeptName())
                || !Objects.equals(apply.getRemark(), request.getRemark())) {
            throw new BusinessException("Review data does not match apply data");
        }

        Department department = new Department();
        department.setDeptName(request.getDeptName());
        department.setRemark(request.getRemark());
        department.setDeptStatus(apply.getDeptStatus());
        department.setCreatedOperName(reviewer.getOperName());
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedOperName(reviewer.getOperName());
        department.setUpdatedAt(LocalDateTime.now());
        department.setReviewOperName(reviewer.getOperName());
        department.setReviewTime(LocalDateTime.now());
        departmentMapper.insert(department);

        if (department.getId() != null) {
            departmentApplyMapper.updateDeptId(apply.getId(), department.getId());
        }
        departmentApplyMapper.updateStatus(
                apply.getId(),
                APPLY_APPROVED,
                reviewer.getOperCode(),
                reviewer.getOperName(),
                LocalDateTime.now(),
                null
        );
    }

    public List<DepartmentExportRow> buildDepartmentExport(String name, String status) {
        return queryDepartments(name, status).stream().map(item -> {
            DepartmentExportRow row = new DepartmentExportRow();
            row.setId(item.getId());
            row.setDeptName(item.getDeptName());
            row.setDeptStatus(item.getDeptStatus());
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

    public List<DepartmentApplyExportRow> buildApplyExport(DepartmentApplyQueryRequest request) {
        return queryPendingDepartmentApplies(request).stream().map(item -> {
            DepartmentApplyExportRow row = new DepartmentApplyExportRow();
            row.setArrNo(item.getArrNo());
            row.setDeptName(item.getDeptName());
            row.setOperType(item.getOperType());
            row.setArrStatus(item.getArrStatus());
            row.setArrOperName(item.getArrOperName());
            row.setArrDate(item.getArrDate());
            row.setReviewOperName(item.getReviewOperName());
            row.setReviewOperCode(item.getReviewOperCode());
            row.setReviewTime(item.getReviewTime());
            return row;
        }).toList();
    }

    private List<DepartmentApplySummary> queryDepartmentAppliesByStatuses(DepartmentApplyQueryRequest request,
                                                                           List<String> statuses) {
        LocalDateTime startTime = parseStartTime(request.getStartDate());
        LocalDateTime endTime = parseEndTime(request.getEndDate());
        return departmentApplyMapper.selectByCondition(
                statuses,
                startTime,
                endTime,
                request.getArrNo(),
                request.getDeptName(),
                request.getOperType()
        );
    }

    private void saveDeptButtonPermissions(Long deptId,
                                           List<ButtonAuthItem> assignAuth,
                                           List<ButtonAuthItem> operAuth) {
        if (deptId == null) {
            throw new BusinessException("Department id is required for permission persistence");
        }
        List<DepartmentButtonPermission> items = new java.util.ArrayList<>();
        Set<String> dedup = new LinkedHashSet<>();
        appendPermissions(items, dedup, deptId, operAuth, PERMISSION_TYPE_OPER);
        appendPermissions(items, dedup, deptId, assignAuth, PERMISSION_TYPE_ASSIGN);
        if (!items.isEmpty()) {
            departmentButtonPermissionMapper.insertBatch(items);
        }
    }

    private void appendPermissions(List<DepartmentButtonPermission> items,
                                   Set<String> dedup,
                                   Long deptId,
                                   List<ButtonAuthItem> authItems,
                                   String permissionType) {
        if (authItems == null || authItems.isEmpty()) {
            return;
        }
        for (ButtonAuthItem authItem : authItems) {
            if (authItem == null || authItem.getBtnId() == null) {
                continue;
            }
            String key = permissionType + ":" + authItem.getBtnId();
            if (!dedup.add(key)) {
                continue;
            }
            DepartmentButtonPermission item = new DepartmentButtonPermission();
            item.setDeptId(deptId);
            item.setBtnId(authItem.getBtnId());
            item.setPermissionType(permissionType);
            item.setCreatedAt(LocalDateTime.now());
            items.add(item);
        }
    }


    private DepartmentApply buildApply(String name, String remark, Long deptId, String deptStatus, String operationType,
                                       CurrentLoginUser applicant) {
        DepartmentApply apply = new DepartmentApply();
        apply.setArrNo(generateApplyNo());
        apply.setDeptId(deptId);
        apply.setDeptName(name);
        apply.setRemark(remark);
        apply.setOperType(operationType);
        // 申请创建时默认状态：待复核
        apply.setOperStatus(APPLY_PENDING);
        apply.setDeptStatus(deptStatus);
        fillApplicantInfo(apply, applicant);
        return apply;
    }

    private void fillApplicantInfo(DepartmentApply apply, CurrentLoginUser applicant) {
        apply.setOperCode(applicant.getLoginOperCode());
        apply.setOperName(applicant.getOperName());
        apply.setArrDate(LocalDateTime.now());
    }

    private Department requireDepartment(Long deptId) {
        Department department = departmentMapper.selectById(deptId);
        if (department == null) {
            throw new BusinessException("Department not found");
        }
        return department;
    }

    private DepartmentApply requireApply(Long applyId) {
        DepartmentApply apply = departmentApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("Apply record not found");
        }
        return apply;
    }

    private boolean hasDepartmentChanges(Department department, DepartmentModifyRequest request) {
        return !Objects.equals(department.getDeptName(), request.getDeptName())
                || !Objects.equals(department.getRemark(), request.getRemark());
    }

    private DepartmentSummary toSummary(Department department) {
        DepartmentSummary summary = new DepartmentSummary();
        summary.setId(department.getId());
        summary.setDeptName(department.getDeptName());
        summary.setRemark(department.getRemark());
        summary.setDeptStatus(department.getDeptStatus());
        summary.setCreatedOperName(department.getCreatedOperName());
        summary.setCreatedAt(department.getCreatedAt());
        summary.setUpdatedOperName(department.getUpdatedOperName());
        summary.setUpdatedAt(department.getUpdatedAt());
        summary.setReviewOperName(department.getReviewOperName());
        summary.setReviewTime(department.getReviewTime());
        return summary;
    }

    private void fillDepartmentAuth(DepartmentSummary summary, Long deptId) {
        DepartmentAuth auth = resolveDepartmentAuth(deptId);
        summary.setAssignAuth(auth.assignAuth());
        summary.setOperAuth(auth.operAuth());
    }

    private DepartmentAuth resolveDepartmentAuth(Long deptId) {
        if (deptId == null) {
            return DepartmentAuth.empty();
        }
        List<DepartmentButtonPermission> permissions = departmentButtonPermissionMapper.selectByDeptId(deptId);
        if (permissions == null || permissions.isEmpty()) {
            return DepartmentAuth.empty();
        }
        List<ButtonAuthItem> assignAuth = new java.util.ArrayList<>();
        List<ButtonAuthItem> operAuth = new java.util.ArrayList<>();
        for (DepartmentButtonPermission permission : permissions) {
            if (permission == null || permission.getBtnId() == null) {
                continue;
            }
            ButtonAuthItem item = new ButtonAuthItem();
            item.setBtnId(permission.getBtnId());
            if (PERMISSION_TYPE_ASSIGN.equals(permission.getPermissionType())) {
                assignAuth.add(item);
            } else if (PERMISSION_TYPE_OPER.equals(permission.getPermissionType())) {
                operAuth.add(item);
            }
        }
        return new DepartmentAuth(assignAuth, operAuth);
    }

    private record DepartmentAuth(List<ButtonAuthItem> assignAuth, List<ButtonAuthItem> operAuth) {
        static DepartmentAuth empty() {
            return new DepartmentAuth(Collections.emptyList(), Collections.emptyList());
        }
    }

    private UserSummary toUserSummary(PlatformUser user) {
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setOperCode(user.getOperCode());
        summary.setOperName(user.getOperName());
        summary.setOperStatus(user.getOperStatus());
        summary.setUserType(user.getUserType());
        summary.setPhone(user.getPhone());
        return summary;
    }

    private void validateDepartmentRequest(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("Department name is required");
        }
    }

    private List<String> resolveApplyStatuses(String statusType) {
        // 待复核分页支持状态筛选：PENDING/REJECTED/CANCELED
        if (statusType == null || statusType.isBlank()) {
            return DEPARTMENT_PENDING_STATUSES;
        }
        if ("PENDING".equalsIgnoreCase(statusType)) {
            return List.of(APPLY_PENDING);
        }
        if ("REJECTED".equalsIgnoreCase(statusType)) {
            return List.of(APPLY_REJECTED);
        }
        if ("APPROVED".equalsIgnoreCase(statusType)) {
            return List.of(APPLY_APPROVED);
        }
        if ("CANCELED".equalsIgnoreCase(statusType)) {
            return List.of(APPLY_CANCELED);
        }
        if ("REVIEWED".equalsIgnoreCase(statusType)) {
            return List.of(APPLY_APPROVED, APPLY_REJECTED, APPLY_CANCELED);
        }
        throw new BusinessException("Invalid apply status type");
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

    private String generateApplyNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return timestamp + random;
    }
}
