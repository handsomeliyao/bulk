package com.liyao.bulk.mapper;

import com.liyao.bulk.model.DepartmentButtonPermission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentButtonPermissionMapper {

    int insertBatch(@Param("items") List<DepartmentButtonPermission> items);

    List<DepartmentButtonPermission> selectByDeptId(@Param("deptId") Long deptId);

    int updateDeptId(@Param("sourceDeptId") Long sourceDeptId, @Param("targetDeptId") Long targetDeptId);

    int deleteByDeptId(@Param("deptId") Long deptId);
}
