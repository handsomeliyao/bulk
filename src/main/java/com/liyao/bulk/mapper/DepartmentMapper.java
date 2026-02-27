package com.liyao.bulk.mapper;

import com.liyao.bulk.model.Department;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentMapper {

    List<Department> selectByCondition(@Param("name") String name,
                                       @Param("status") String status);

    long countByCondition(@Param("name") String name,
                          @Param("status") String status);

    List<Department> selectPageByCondition(@Param("name") String name,
                                           @Param("status") String status,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    Department selectById(@Param("id") Long id);

    Department selectByName(@Param("name") String name);

    int insert(Department department);

    int update(Department department);
}
