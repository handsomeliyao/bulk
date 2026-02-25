package com.liyao.bulk.mapper;

import com.liyao.bulk.model.PositionScope;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PositionScopeMapper {

    List<PositionScope> selectByPositionId(@Param("positionId") Long positionId);

    List<PositionScope> selectByPositionIds(@Param("positionIds") List<Long> positionIds);

    int insertBatch(@Param("scopes") List<PositionScope> scopes);

    int deleteByPositionId(@Param("positionId") Long positionId);
}
