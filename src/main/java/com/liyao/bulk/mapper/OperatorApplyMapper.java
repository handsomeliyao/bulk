package com.liyao.bulk.mapper;

import com.liyao.bulk.dto.OperatorApplySummary;
import com.liyao.bulk.model.OperatorApply;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OperatorApplyMapper {

    int insert(OperatorApply apply);

    OperatorApply selectById(@Param("id") Long id);

    List<OperatorApplySummary> selectByCondition(@Param("deptId") Long deptId,
                                                 @Param("statusList") List<String> statusList,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("arrNo") String arrNo,
                                                 @Param("arrOperName") String arrOperName,
                                                 @Param("operType") String operType);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("reviewOperCode") Long reviewOperCode,
                     @Param("reviewOperName") String reviewOperName,
                     @Param("reviewTime") LocalDateTime reviewTime,
                     @Param("reviewRemark") String reviewRemark);

    int countPendingByUserId(@Param("operCode") Long operCode);

    int countPendingByOperCode(@Param("operName") String operName);
}

