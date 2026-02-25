package com.liyao.bulk.mapper;

import com.liyao.bulk.model.OperatorApplyPosition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OperatorApplyPositionMapper {

    List<OperatorApplyPosition> selectByApplyId(@Param("applyId") Long applyId);

    int insertBatch(@Param("items") List<OperatorApplyPosition> items);
}
