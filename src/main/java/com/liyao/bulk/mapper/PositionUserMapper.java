package com.liyao.bulk.mapper;

import com.liyao.bulk.model.PositionUser;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PositionUserMapper {

    List<PositionUser> selectByOperCode(@Param("operCode") Long operCode);

    int deleteByOperCode(@Param("operCode") Long operCode);

    int insertBatch(@Param("items") List<PositionUser> items);
}
