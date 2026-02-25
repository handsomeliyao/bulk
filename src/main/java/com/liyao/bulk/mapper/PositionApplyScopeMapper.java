package com.liyao.bulk.mapper;

import com.liyao.bulk.model.PositionApplyScope;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PositionApplyScopeMapper {

    List<PositionApplyScope> selectByApplyId(@Param("applyId") Long applyId);

    int insertBatch(@Param("scopes") List<PositionApplyScope> scopes);
}
