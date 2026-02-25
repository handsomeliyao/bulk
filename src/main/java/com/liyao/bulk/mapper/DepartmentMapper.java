package com.liyao.bulk.mapper;

import com.liyao.bulk.model.Department;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentMapper {

    List<Department> selectByCondition(@Param("name") String name,
                                       @Param("status") String status);

    Department selectById(@Param("id") Long id);

    Department selectByName(@Param("name") String name);

    int insert(Department department);

    int update(Department department);
}
