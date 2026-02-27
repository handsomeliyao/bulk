package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.dto.*;
import com.liyao.bulk.mapper.DepartmentApplyMapper;
import com.liyao.bulk.mapper.DepartmentButtonPermissionMapper;
import com.liyao.bulk.mapper.DepartmentMapper;
import com.liyao.bulk.mapper.PermissionMapper;
import com.liyao.bulk.mapper.PlatformUserMapper;
import com.liyao.bulk.model.DepartmentButtonPermission;
import com.liyao.bulk.model.Department;
import com.liyao.bulk.model.DepartmentApply;
import com.liyao.bulk.model.PlatformUser;
import com.liyao.bulk.model.SysButton;
import com.liyao.bulk.model.SysMenu;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_CANCELED = "CANCELED";

    private static final String APPLY_PENDING = "PENDING";
    private static final String APPLY_APPROVED = "APPROVED";
    private static final String APPLY_REJECTED = "REJECTED";
    private static final String APPLY_CANCELED = "CANCELED";

    // 部门申请状态分组
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
    private final PermissionMapper permissionMapper;
    private final PlatformUserMapper platformUserMapper;
    private final LoginUserCacheService loginUserCacheService;

    public DepartmentService(DepartmentMapper departmentMapper,
                             DepartmentApplyMapper departmentApplyMapper,
                             DepartmentButtonPermissionMapper departmentButtonPermissionMapper,
                             PermissionMapper permissionMapper,
                             PlatformUserMapper platformUserMapper,
                             LoginUserCacheService loginUserCacheService) {
        this.departmentMapper = departmentMapper;
        this.departmentApplyMapper = departmentApplyMapper;
        this.departmentButtonPermissionMapper = departmentButtonPermissionMapper;
        this.permissionMapper = permissionMapper;
        this.platformUserMapper = platformUserMapper;
        this.loginUserCacheService = loginUserCacheService;
    }

    public PageResult<DepartmentSummary> queryDepartments(String name, String status,
                                                          Integer pageNum, Integer pageSize) {
        int safePageNum = pageNum == null || pageNum <= 0 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;
        int offset = (safePageNum - 1) * safePageSize;

        long total = departmentMapper.countByCondition(name, status);
        List<DepartmentSummary> list = queryDepartmentsAll(name, status, offset, safePageSize);

        PageResult<DepartmentSummary> result = new PageResult<>();
        result.setTotal(total);
        result.setPageNum(safePageNum);
        result.setPageSize(safePageSize);
        result.setList(list);
        return result;
    }

    public List<DepartmentOption> queryNormalDepartments() {
        return departmentMapper.selectByCondition(null, STATUS_NORMAL).stream().map(department -> {
            DepartmentOption option = new DepartmentOption();
            option.setId(department.getId());
            option.setDeptName(department.getDeptName());
            return option;
        }).toList();
    }

    private List<DepartmentSummary> queryDepartmentsAll(String name, String status, int offset, int limit) {
        List<Department> departments = departmentMapper.selectPageByCondition(name, status, offset, limit);
        return departments.stream().map(department -> {
            DepartmentSummary summary = toSummary(department);
            fillDepartmentAuth(summary, department.getId());
            return summary;
        }).toList();
    }

    public DepartmentDetailResponse getDepartmentDetail(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
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
        DepartmentAuth auth = resolveDepartmentAuth(department.getId());
        response.setAssignAuth(auth.assignAuth());
        response.setOperAuth(auth.operAuth());
        return response;
    }

    public List<UserSummary> queryDepartmentUsers(Long deptId) {
        List<PlatformUser> users = platformUserMapper.selectByDeptId(deptId);
        return users.stream().map(this::toUserSummary).toList();
    }

    public List<PermissionMenuTreeItem> queryDepartmentOperPermissionTree(Long deptId) {
        return queryDepartmentPermissionTreeByType(deptId, PERMISSION_TYPE_OPER);
    }

    public List<PermissionMenuTreeItem> queryDepartmentAssignPermissionTree(Long deptId) {
        return queryDepartmentPermissionTreeByType(deptId, PERMISSION_TYPE_ASSIGN);
    }

    private List<PermissionMenuTreeItem> queryDepartmentPermissionTreeByType(Long deptId, String permissionType) {
        requireDepartment(deptId);
        DepartmentAuth auth = resolveDepartmentAuth(deptId);
        List<ButtonAuthItem> source = PERMISSION_TYPE_OPER.equals(permissionType) ? auth.operAuth() : auth.assignAuth();
        Set<Long> selectedButtonIds = source.stream()
                .map(ButtonAuthItem::getBtnId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<SysMenu> menus = permissionMapper.selectAllMenus();
        List<SysButton> buttons = permissionMapper.selectAllButtons();
        Map<Long, List<PermissionButtonItem>> buttonMap = buttons.stream()
                .map(this::toPermissionButtonItem)
                .collect(Collectors.groupingBy(
                        PermissionButtonItem::getMenuId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return buildPermissionTree(menus, buttonMap, selectedButtonIds);
    }

    @Transactional
    public void createDepartmentApply(DepartmentCreateRequest request) {
        validateDepartmentRequest(request.getDeptName());
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        if (departmentApplyMapper.countPendingByDeptName(request.getDeptName()) > 0) {
            throw new BusinessException("同一部门存在待复核申请，不允许重复发起");
        }
        Department existing = departmentMapper.selectByName(request.getDeptName());
        if (existing != null) {
            throw new BusinessException("部门名称已存在");
        }
        DepartmentApply apply = buildApply(request.getDeptName(), request.getRemark(), null,
                STATUS_NORMAL, OP_ADD, applicant);
        departmentApplyMapper.insert(apply);
        if (apply.getId() == null) {
            throw new BusinessException("申请保存失败，未返回申请ID");
        }
        departmentApplyMapper.updateDeptId(apply.getId(), apply.getId());
        apply.setDeptId(apply.getId());
        saveDeptButtonPermissions(apply.getDeptId(), request.getAssignAuth(), request.getOperAuth());
    }

    @Transactional
    public void modifyDepartmentApply(Long deptId, DepartmentModifyRequest request) {
        validateDepartmentRequest(request.getDeptName());
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        Department department = requireDepartment(deptId);
        if (!STATUS_NORMAL.equals(department.getDeptStatus())) {
            throw new BusinessException("当前部门状态不允许修改");
        }
        if (departmentApplyMapper.countPendingByDeptId(deptId) > 0) {
            throw new BusinessException("同一部门存在待复核申请，不允许重复发起");
        }
        Department existing = departmentMapper.selectByName(request.getDeptName());
        if (existing != null && !existing.getId().equals(deptId)) {
            throw new BusinessException("部门名称已存在");
        }
        if (!hasDepartmentChanges(department, request)) {
            throw new BusinessException("未检测到可提交的修改内容");
        }
        DepartmentApply apply = buildApply(request.getDeptName(), request.getRemark(), deptId, department.getDeptStatus(), OP_MODIFY, applicant);
        departmentApplyMapper.insert(apply);
    }

    @Transactional
    public void cancelDepartmentApply(Long deptId) {
        CurrentLoginUser applicant = loginUserCacheService.getRequiredCurrentUser();
        Department department = requireDepartment(deptId);
        if (!STATUS_NORMAL.equals(department.getDeptStatus())) {
            throw new BusinessException("当前部门状态不允许注销");
        }
        if (departmentApplyMapper.countPendingByDeptId(deptId) > 0) {
            throw new BusinessException("同一部门存在待复核申请，不允许重复发起");
        }
        DepartmentApply apply = new DepartmentApply();
        apply.setArrNo(generateApplyNo());
        apply.setDeptId(deptId);
        apply.setDeptName(department.getDeptName());
        apply.setRemark(department.getRemark());
        apply.setOperType(OP_CANCEL);
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

    public DepartmentApplyDetailResponse getApplyDetailByArrNo(String arrNo) {
        if (arrNo == null || arrNo.isBlank()) {
            throw new BusinessException("申请编号不能为空");
        }
        DepartmentApply apply = departmentApplyMapper.selectByArrNo(arrNo);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        DepartmentApplyDetailResponse response = new DepartmentApplyDetailResponse();
        response.setId(apply.getId());
        response.setArrNo(apply.getArrNo());
        response.setDeptId(apply.getDeptId());
        response.setDeptName(apply.getDeptName());
        response.setRemark(apply.getRemark());
        response.setOperType(apply.getOperType());
        response.setArrStatus(apply.getOperStatus());
        response.setDeptStatus(apply.getDeptStatus());
        response.setArrOperCode(apply.getOperCode());
        response.setArrOperName(apply.getOperName());
        response.setArrDate(apply.getArrDate());
        response.setReviewOperCode(apply.getReviewOperCode());
        response.setReviewOperName(apply.getReviewOperName());
        response.setReviewTime(apply.getReviewTime());
        DepartmentAuth auth = resolveDepartmentAuth(apply.getDeptId());
        response.setAssignAuth(auth.assignAuth());
        response.setOperAuth(auth.operAuth());
        return response;
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
            throw new BusinessException("复核参数applyId不能为空");
        }
        CurrentLoginUser reviewer = loginUserCacheService.getRequiredCurrentUser();
        DepartmentApply apply = requireApply(request.getApplyId());
        Long applyDeptId = apply.getDeptId();
        if (!APPLY_PENDING.equals(apply.getOperStatus())) {
            throw new BusinessException("当前申请状态不是待复核，无法处理");
        }
        if (Objects.equals(apply.getOperCode(), reviewer.getLoginOperCode())) {
            throw new BusinessException("申请人与复核人不能为同一人");
        }
        boolean approved = request.getApproved() == null || request.getApproved();
        if (!approved) {
            departmentApplyMapper.updateStatus(
                    apply.getId(),
                    APPLY_REJECTED,
                    reviewer.getOperCode(),
                    reviewer.getOperName(),
                    LocalDateTime.now(),
                    request.getReviewRemark()
            );
            return;
        }
        if (!OP_ADD.equals(apply.getOperType())
                && !OP_MODIFY.equals(apply.getOperType())
                && !OP_CANCEL.equals(apply.getOperType())) {
            throw new BusinessException("仅支持新增/修改/注销申请的通过复核");
        }

        if (OP_ADD.equals(apply.getOperType())) {
            validateDepartmentRequest(request.getDeptName());
            if (!Objects.equals(apply.getDeptName(), request.getDeptName())
                    || !Objects.equals(apply.getRemark(), request.getRemark())) {
                throw new BusinessException("复核通过时提交内容与申请内容不一致");
            }
            Department department = new Department();
            department.setDeptName(request.getDeptName());
            department.setRemark(request.getRemark());
            department.setDeptStatus(apply.getDeptStatus());
            department.setCreatedOperName(apply.getOperName());
            department.setCreatedAt(apply.getArrDate());
            department.setUpdatedOperName(apply.getOperName());
            department.setUpdatedAt(apply.getArrDate());
            department.setReviewOperName(reviewer.getOperName());
            department.setReviewTime(LocalDateTime.now());
            departmentMapper.insert(department);

            if (department.getId() != null) {
                departmentApplyMapper.updateDeptId(apply.getId(), department.getId());
                if (applyDeptId != null && !applyDeptId.equals(department.getId())) {
                    departmentButtonPermissionMapper.updateDeptId(applyDeptId, department.getId());
                }
                replaceDeptButtonPermissions(department.getId(), request.getAssignAuth(), request.getOperAuth());
            }
        } else if (OP_MODIFY.equals(apply.getOperType())) {
            validateDepartmentRequest(request.getDeptName());
            if (!Objects.equals(apply.getDeptName(), request.getDeptName())
                    || !Objects.equals(apply.getRemark(), request.getRemark())) {
                throw new BusinessException("复核通过时提交内容与申请内容不一致");
            }
            Department department = requireDepartment(apply.getDeptId());
            department.setDeptName(request.getDeptName());
            department.setRemark(request.getRemark());
            department.setDeptStatus(apply.getDeptStatus());
            department.setUpdatedOperName(apply.getOperName());
            department.setUpdatedAt(apply.getArrDate());
            department.setReviewOperName(reviewer.getOperName());
            department.setReviewTime(LocalDateTime.now());
            departmentMapper.update(department);
            replaceDeptButtonPermissions(department.getId(), request.getAssignAuth(), request.getOperAuth());
        } else {
            Department department = requireDepartment(apply.getDeptId());
            department.setDeptStatus(STATUS_CANCELED);
            department.setUpdatedOperName(apply.getOperName());
            department.setUpdatedAt(apply.getArrDate());
            department.setReviewOperName(reviewer.getOperName());
            department.setReviewTime(LocalDateTime.now());
            departmentMapper.update(department);
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
        return departmentMapper.selectByCondition(name, status).stream().map(this::toSummary).map(item -> {
            fillDepartmentAuth(item, item.getId());
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
        List<String> effectiveStatuses = resolveApplyStatusesForQuery(request, statuses);
        return departmentApplyMapper.selectByCondition(
                effectiveStatuses,
                startTime,
                endTime,
                request.getArrNo(),
                request.getDeptName(),
                request.getOperType()
        );
    }

    private List<String> resolveApplyStatusesForQuery(DepartmentApplyQueryRequest request, List<String> defaultStatuses) {
        if (request == null) {
            return defaultStatuses;
        }
        String requested = request.getArrStatus();
        if (requested == null) {
            return defaultStatuses;
        }
        String normalized = requested.trim().toUpperCase();
        if (defaultStatuses.contains(normalized)) {
            return List.of(normalized);
        }
        // 鍏滃簳锛氳繑鍥炰竴涓笉浼氬懡涓殑鐘舵€侊紝閬垮厤璇煡鍏ㄩ噺鏁版嵁
        return List.of("__NO_MATCH_STATUS__");
    }
    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private void saveDeptButtonPermissions(Long deptId,
                                           List<ButtonAuthItem> assignAuth,
                                           List<ButtonAuthItem> operAuth) {
        if (deptId == null) {
            throw new BusinessException("部门ID不能为空，无法保存权限");
        }
        List<DepartmentButtonPermission> items = new java.util.ArrayList<>();
        Set<String> dedup = new LinkedHashSet<>();
        appendPermissions(items, dedup, deptId, operAuth, PERMISSION_TYPE_OPER);
        appendPermissions(items, dedup, deptId, assignAuth, PERMISSION_TYPE_ASSIGN);
        if (!items.isEmpty()) {
            departmentButtonPermissionMapper.insertBatch(items);
        }
    }

    private void replaceDeptButtonPermissions(Long deptId,
                                              List<ButtonAuthItem> assignAuth,
                                              List<ButtonAuthItem> operAuth) {
        if (deptId == null) {
            throw new BusinessException("部门ID不能为空，无法保存权限");
        }
        departmentButtonPermissionMapper.deleteByDeptId(deptId);
        saveDeptButtonPermissions(deptId, assignAuth, operAuth);
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
            throw new BusinessException("部门不存在");
        }
        return department;
    }

    private DepartmentApply requireApply(Long applyId) {
        DepartmentApply apply = departmentApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
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

    private List<PermissionMenuTreeItem> buildPermissionTree(List<SysMenu> menus,
                                                             Map<Long, List<PermissionButtonItem>> buttonMap,
                                                             Set<Long> selectedButtonIds) {
        if (menus == null || menus.isEmpty() || selectedButtonIds == null || selectedButtonIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, PermissionMenuTreeItem> nodeMap = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            PermissionMenuTreeItem node = new PermissionMenuTreeItem();
            node.setId(menu.getId());
            node.setPid(menu.getPid());
            node.setMenuCode(menu.getMenuCode());
            node.setMenuName(menu.getMenuName());
            node.setMenuOrder(menu.getMenuOrder());
            node.setIcon(menu.getIcon());
            node.setUrl(menu.getUrl());
            List<PermissionButtonItem> selectedButtons = buttonMap
                    .getOrDefault(menu.getId(), Collections.emptyList())
                    .stream()
                    .filter(item -> selectedButtonIds.contains(item.getId()))
                    .toList();
            // Backward compatibility: some historical records store menu ids in dept_btn.btn_id.
            // If current menu is selected by id and no explicit button ids are found, include all menu buttons.
            if (selectedButtons.isEmpty() && selectedButtonIds.contains(menu.getId())) {
                selectedButtons = buttonMap.getOrDefault(menu.getId(), Collections.emptyList());
            }
            node.setButtons(selectedButtons.isEmpty() ? null : selectedButtons);
            node.setChildren(new ArrayList<>());
            nodeMap.put(menu.getId(), node);
        }

        List<PermissionMenuTreeItem> roots = new ArrayList<>();
        for (PermissionMenuTreeItem node : nodeMap.values()) {
            Long pid = node.getPid();
            if (pid == null || pid == 0L) {
                roots.add(node);
                continue;
            }
            PermissionMenuTreeItem parent = nodeMap.get(pid);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }

        pruneEmptyMenuNodes(roots);
        sortPermissionTree(roots);
        return roots;
    }

    private void pruneEmptyMenuNodes(List<PermissionMenuTreeItem> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        java.util.Iterator<PermissionMenuTreeItem> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            PermissionMenuTreeItem node = iterator.next();
            pruneEmptyMenuNodes(node.getChildren());
            boolean hasChildren = node.getChildren() != null && !node.getChildren().isEmpty();
            boolean hasButtons = node.getButtons() != null && !node.getButtons().isEmpty();
            if (!hasChildren && !hasButtons) {
                iterator.remove();
                continue;
            }
            if (hasChildren) {
                node.setButtons(null);
            }
        }
    }

    private void sortPermissionTree(List<PermissionMenuTreeItem> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(Comparator
                .comparing(PermissionMenuTreeItem::getMenuOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(PermissionMenuTreeItem::getId));
        for (PermissionMenuTreeItem node : nodes) {
            sortPermissionTree(node.getChildren());
        }
    }

    private PermissionButtonItem toPermissionButtonItem(SysButton button) {
        PermissionButtonItem item = new PermissionButtonItem();
        item.setId(button.getId());
        item.setMenuId(button.getMenuId());
        item.setBtnCode(button.getBtnCode());
        item.setBtnName(button.getBtnName());
        item.setMethod(button.getMethod());
        item.setUri(button.getUri());
        return item;
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
            throw new BusinessException("部门名称不能为空");
        }
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



