package com.liyao.bulk.mapper;

import com.liyao.bulk.model.DepartmentUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentUserMapper {

    int insert(DepartmentUser departmentUser);

    Long selectDeptIdByUserId(@Param("userId") Long userId);

    int deleteByUserId(@Param("userId") Long userId);
}
