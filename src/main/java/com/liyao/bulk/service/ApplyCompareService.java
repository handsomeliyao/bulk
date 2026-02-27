package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.dto.AdminDetailResponse;
import com.liyao.bulk.dto.ButtonAuthItem;
import com.liyao.bulk.dto.DepartmentDetailResponse;
import com.liyao.bulk.dto.OperatorDetailResponse;
import com.liyao.bulk.dto.PositionDetailResponse;
import com.liyao.bulk.mapper.AdminApplyMapper;
import com.liyao.bulk.mapper.DepartmentApplyMapper;
import com.liyao.bulk.mapper.DepartmentButtonPermissionMapper;
import com.liyao.bulk.mapper.DepartmentMapper;
import com.liyao.bulk.mapper.OperatorApplyMapper;
import com.liyao.bulk.mapper.PlatformUserMapper;
import com.liyao.bulk.mapper.PositionApplyMapper;
import com.liyao.bulk.mapper.PositionMapper;
import com.liyao.bulk.model.AdminApply;
import com.liyao.bulk.model.Department;
import com.liyao.bulk.model.DepartmentApply;
import com.liyao.bulk.model.DepartmentButtonPermission;
import com.liyao.bulk.model.OperatorApply;
import com.liyao.bulk.model.PlatformUser;
import com.liyao.bulk.model.Position;
import com.liyao.bulk.model.PositionApply;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ApplyCompareService {

    private static final String OP_ADD = "ADD";
    private static final String OP_MODIFY = "MODIFY";
    private static final String OP_CANCEL = "CANCEL";
    private static final String PERMISSION_TYPE_OPER = "1";
    private static final String PERMISSION_TYPE_ASSIGN = "2";

    private final DepartmentApplyMapper departmentApplyMapper;
    private final DepartmentMapper departmentMapper;
    private final DepartmentButtonPermissionMapper departmentButtonPermissionMapper;
    private final PositionApplyMapper positionApplyMapper;
    private final PositionMapper positionMapper;
    private final AdminApplyMapper adminApplyMapper;
    private final OperatorApplyMapper operatorApplyMapper;
    private final PlatformUserMapper platformUserMapper;

    public ApplyCompareService(DepartmentApplyMapper departmentApplyMapper,
                               DepartmentMapper departmentMapper,
                               DepartmentButtonPermissionMapper departmentButtonPermissionMapper,
                               PositionApplyMapper positionApplyMapper,
                               PositionMapper positionMapper,
                               AdminApplyMapper adminApplyMapper,
                               OperatorApplyMapper operatorApplyMapper,
                               PlatformUserMapper platformUserMapper) {
        this.departmentApplyMapper = departmentApplyMapper;
        this.departmentMapper = departmentMapper;
        this.departmentButtonPermissionMapper = departmentButtonPermissionMapper;
        this.positionApplyMapper = positionApplyMapper;
        this.positionMapper = positionMapper;
        this.adminApplyMapper = adminApplyMapper;
        this.operatorApplyMapper = operatorApplyMapper;
        this.platformUserMapper = platformUserMapper;
    }

    public DepartmentDetailResponse compareDepartment(String applyIdOrArrNo) {
        DepartmentApply apply = resolveDepartmentApply(applyIdOrArrNo);
        String operationType = apply.getOperType();
        Department formal = (OP_MODIFY.equals(operationType) || OP_CANCEL.equals(operationType))
                ? departmentMapper.selectById(apply.getDeptId()) : null;
        if ((OP_MODIFY.equals(operationType) || OP_CANCEL.equals(operationType)) && formal == null) {
            throw new BusinessException("正式部门不存在");
        }

        DepartmentDetailResponse response = new DepartmentDetailResponse();
        response.setId(formal == null ? null : formal.getId());
        response.setDeptName(compareOrNull(formal == null ? null : formal.getDeptName(), apply.getDeptName()));
        response.setDeptStatus(compareOrNull(formal == null ? null : formal.getDeptStatus(), apply.getDeptStatus()));
        response.setRemark(compareOrNull(formal == null ? null : formal.getRemark(), apply.getRemark()));
        response.setCreatedOperName(null);
        response.setCreatedAt(null);
        response.setUpdatedOperName(null);
        response.setUpdatedAt(null);
        response.setReviewOperName(null);
        response.setReviewTime(null);
        if (formal == null) {
            response.setAssignAuth(null);
            response.setOperAuth(null);
        } else {
            DepartmentAuth auth = resolveDepartmentAuth(formal.getId());
            response.setAssignAuth(auth.assignAuth());
            response.setOperAuth(auth.operAuth());
        }
        return response;
    }

    public PositionDetailResponse comparePosition(Long applyId) {
        PositionApply apply = positionApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        String operationType = apply.getOperType();
        Position formal = (OP_MODIFY.equals(operationType) || OP_CANCEL.equals(operationType))
                ? positionMapper.selectById(apply.getPostId()) : null;
        if ((OP_MODIFY.equals(operationType) || OP_CANCEL.equals(operationType)) && formal == null) {
            throw new BusinessException("岗位不存在");
        }

        PositionDetailResponse response = new PositionDetailResponse();
        response.setId(formal == null ? null : formal.getId());
        response.setDeptId(compareOrNull(formal == null ? null : formal.getDeptId(), apply.getDeptId()));
        response.setDeptName(compareOrNull(formal == null ? null : formal.getDeptName(), apply.getDeptName()));
        response.setPostName(compareOrNull(formal == null ? null : formal.getPostName(), apply.getPostName()));
        response.setType(compareOrNull(formal == null ? null : formal.getPostType(), apply.getPostType()));
        response.setPostStatus(formal == null ? null : formal.getPostStatus());
        response.setRemark(compareOrNull(formal == null ? null : formal.getRemark(), apply.getRemark()));
        response.setCreatedOperName(null);
        response.setCreatedAt(null);
        response.setUpdatedOperName(null);
        response.setUpdatedAt(null);
        response.setReviewOperName(null);
        response.setReviewTime(null);
        response.setOperAuth(Collections.emptyList());
        return response;
    }

    public AdminDetailResponse compareAdmin(Long applyId) {
        AdminApply apply = adminApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        String operationType = apply.getOperationType();
        PlatformUser formal = (OP_MODIFY.equals(operationType) || OP_CANCEL.equals(operationType))
                ? platformUserMapper.selectById(apply.getOperCode()) : null;
        if ((OP_MODIFY.equals(operationType) || OP_CANCEL.equals(operationType)) && formal == null) {
            throw new BusinessException("正式管理员不存在");
        }

        AdminDetailResponse response = new AdminDetailResponse();
        response.setId(formal == null ? null : formal.getId());
        response.setDeptId(compareOrNull(formal == null ? null : formal.getDeptId(), apply.getDeptId()));
        response.setDeptName(resolveDeptName(response.getDeptId()));
        response.setOperCode(compareOrNull(formal == null ? null : formal.getOperCode(), apply.getOperName()));
        response.setOperName(formal == null ? null : formal.getOperName());
        response.setUserType(compareOrNull(formal == null ? null : formal.getUserType(), apply.getOperType()));
        response.setOperStatus(formal == null ? null : formal.getOperStatus());
        response.setTelPhone(compareOrNull(formal == null ? null : formal.getTelPhone(), apply.getTelPhone()));
        response.setPhone(compareOrNull(formal == null ? null : formal.getPhone(), apply.getMobile()));
        response.setRemark(compareOrNull(formal == null ? null : formal.getRemark(), apply.getRemark()));
        response.setCreatedOperName(null);
        response.setCreatedAt(null);
        response.setUpdatedOperName(null);
        response.setUpdatedAt(null);
        response.setReviewOperName(null);
        response.setReviewTime(null);
        response.setAssignAuth(Collections.emptyList());
        response.setOperAuth(Collections.emptyList());
        return response;
    }

    public OperatorDetailResponse compareOperator(Long applyId) {
        OperatorApply apply = operatorApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        String operationType = apply.getOperType();
        PlatformUser formal = (OP_MODIFY.equals(operationType) || OP_CANCEL.equals(operationType))
                ? platformUserMapper.selectById(apply.getOperCode()) : null;
        if ((OP_MODIFY.equals(operationType) || OP_CANCEL.equals(operationType)) && formal == null) {
            throw new BusinessException("正式操作员不存在");
        }

        OperatorDetailResponse response = new OperatorDetailResponse();
        response.setId(formal == null ? null : formal.getId());
        response.setDeptId(compareOrNull(formal == null ? null : formal.getDeptId(), apply.getDeptId()));
        response.setDeptName(compareOrNull(formal == null ? null : resolveDeptName(formal.getDeptId()), apply.getDeptName()));
        response.setOperCode(compareOrNull(formal == null ? null : formal.getOperCode(), apply.getOperName()));
        response.setOperName(formal == null ? null : formal.getOperName());
        response.setUserType(formal == null ? null : formal.getUserType());
        response.setOperStatus(formal == null ? null : formal.getOperStatus());
        response.setTelPhone(compareOrNull(formal == null ? null : formal.getTelPhone(), apply.getTelPhone()));
        response.setPhone(compareOrNull(formal == null ? null : formal.getPhone(), apply.getMobile()));
        response.setRemark(compareOrNull(formal == null ? null : formal.getRemark(), apply.getRemark()));
        response.setCreatedOperName(null);
        response.setCreatedAt(null);
        response.setUpdatedOperName(null);
        response.setUpdatedAt(null);
        response.setReviewOperName(null);
        response.setReviewTime(null);
        response.setPositions(Collections.emptyList());
        response.setPermissions(Collections.emptyList());
        return response;
    }

    private DepartmentApply resolveDepartmentApply(String applyIdOrArrNo) {
        if (applyIdOrArrNo == null || applyIdOrArrNo.isBlank()) {
            throw new BusinessException("申请记录不存在");
        }
        String key = applyIdOrArrNo.trim();
        DepartmentApply apply = departmentApplyMapper.selectByArrNo(key);
        if (apply != null) {
            return apply;
        }
        try {
            apply = departmentApplyMapper.selectById(Long.parseLong(key));
            if (apply != null) {
                return apply;
            }
        } catch (NumberFormatException ex) {
            throw new BusinessException("申请记录不存在");
        }
        throw new BusinessException("申请记录不存在");
    }

    private String resolveDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        Department department = departmentMapper.selectById(deptId);
        return department == null ? null : department.getDeptName();
    }

    private DepartmentAuth resolveDepartmentAuth(Long deptId) {
        if (deptId == null) {
            return DepartmentAuth.empty();
        }
        List<DepartmentButtonPermission> permissions = departmentButtonPermissionMapper.selectByDeptId(deptId);
        if (permissions == null || permissions.isEmpty()) {
            return DepartmentAuth.empty();
        }
        List<ButtonAuthItem> assignAuth = new ArrayList<>();
        List<ButtonAuthItem> operAuth = new ArrayList<>();
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

    private <T> T compareOrNull(T formalValue, T applyValue) {
        if (Objects.equals(formalValue, applyValue)) {
            return applyValue;
        }
        return null;
    }

    private record DepartmentAuth(List<ButtonAuthItem> assignAuth, List<ButtonAuthItem> operAuth) {
        static DepartmentAuth empty() {
            return new DepartmentAuth(Collections.emptyList(), Collections.emptyList());
        }
    }
}



