package com.liyao.bulk.mapper;

import com.liyao.bulk.model.UserButtonPermission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserButtonPermissionMapper {

    int insertBatch(@Param("items") List<UserButtonPermission> items);

    List<UserButtonPermission> selectByUserId(@Param("userId") Long userId);

    int updateUserId(@Param("sourceUserId") Long sourceUserId, @Param("targetUserId") Long targetUserId);

    int deleteByUserId(@Param("userId") Long userId);
}
