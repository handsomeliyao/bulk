package com.liyao.bulk.mapper;

import com.liyao.bulk.dto.PositionApplySummary;
import com.liyao.bulk.model.PositionApply;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PositionApplyMapper {

    int insert(PositionApply apply);

    PositionApply selectById(@Param("id") Long id);

    List<PositionApplySummary> selectByCondition(@Param("deptId") Long deptId,
                                                 @Param("statusList") List<String> statusList,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("arrNo") String arrNo,
                                                 @Param("postName") String postName,
                                                 @Param("operType") String operType);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("reviewOperCode") Long reviewOperCode,
                     @Param("reviewOperName") String reviewOperName,
                     @Param("reviewTime") LocalDateTime reviewTime,
                     @Param("remark") String remark);

    int countPendingByPositionId(@Param("postId") Long postId);

    int countPendingByDeptAndName(@Param("deptId") Long deptId,
                                  @Param("postName") String postName);
}

