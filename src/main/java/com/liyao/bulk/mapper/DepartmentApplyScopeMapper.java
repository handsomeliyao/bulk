package com.liyao.bulk.mapper;

import com.liyao.bulk.model.DepartmentApplyScope;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentApplyScopeMapper {

    List<DepartmentApplyScope> selectByApplyId(@Param("applyId") Long applyId);

    int insertBatch(@Param("scopes") List<DepartmentApplyScope> scopes);
}
