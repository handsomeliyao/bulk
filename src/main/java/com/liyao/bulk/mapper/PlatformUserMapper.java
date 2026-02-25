package com.liyao.bulk.mapper;

import com.liyao.bulk.model.PlatformUser;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlatformUserMapper {

    List<PlatformUser> selectByDeptId(@Param("deptId") Long deptId);

    List<PlatformUser> selectByPositionId(@Param("positionId") Long positionId);

    PlatformUser selectById(@Param("id") Long id);

    PlatformUser selectByOperCode(@Param("operCode") String operCode);

    List<PlatformUser> selectOperatorsByCondition(@Param("deptId") Long deptId,
                                                  @Param("operCode") String operCode,
                                                  @Param("operName") String operName,
                                                  @Param("operStatus") String operStatus,
                                                  @Param("userType") String userType);

    int insert(PlatformUser user);

    int update(PlatformUser user);

    int updateStatus(@Param("id") Long id,
                     @Param("operStatus") String operStatus);
}
