package com.liyao.bulk.mapper;

import com.liyao.bulk.model.DepartmentScope;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentScopeMapper {

    List<DepartmentScope> selectByDeptId(@Param("deptId") Long deptId);

    int insertBatch(@Param("scopes") List<DepartmentScope> scopes);

    int deleteByDeptId(@Param("deptId") Long deptId);
}
