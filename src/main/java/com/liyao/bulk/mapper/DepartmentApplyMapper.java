package com.liyao.bulk.mapper;

import com.liyao.bulk.dto.DepartmentApplySummary;
import com.liyao.bulk.model.DepartmentApply;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentApplyMapper {

    int insert(DepartmentApply apply);

    DepartmentApply selectById(@Param("id") Long id);

    DepartmentApply selectByArrNo(@Param("arrNo") String arrNo);

    List<DepartmentApplySummary> selectByCondition(@Param("statusList") List<String> statusList,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("arrNo") String arrNo,
                                                   @Param("deptName") String deptName,
                                                   @Param("operType") String operType);

    int updateDeptId(@Param("id") Long id, @Param("deptId") Long deptId);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("reviewOperCode") Long reviewOperCode,
                     @Param("reviewOperName") String reviewOperName,
                     @Param("reviewTime") LocalDateTime reviewTime,
                     @Param("remark") String remark);

    int countPendingByDeptId(@Param("deptId") Long deptId);

    int countPendingByDeptName(@Param("deptName") String deptName);
}

