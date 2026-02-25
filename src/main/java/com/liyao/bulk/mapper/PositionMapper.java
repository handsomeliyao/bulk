package com.liyao.bulk.mapper;

import com.liyao.bulk.model.Position;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PositionMapper {

    List<Position> selectByCondition(@Param("deptId") Long deptId,
                                     @Param("name") String name,
                                     @Param("status") String status,
                                     @Param("type") String type);

    Position selectById(@Param("id") Long id);

    Position selectByNameInDept(@Param("deptId") Long deptId,
                                @Param("name") String name);

    List<Position> selectByDeptAndTypeStatus(@Param("deptId") Long deptId,
                                             @Param("type") String type,
                                             @Param("status") String status);

    List<Position> selectByIds(@Param("ids") List<Long> ids);

    List<Position> selectByUserId(@Param("userId") Long userId);

    int insert(Position position);

    int update(Position position);
}
