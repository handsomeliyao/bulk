package com.liyao.bulk.mapper;

import com.liyao.bulk.model.RequestLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequestLogMapper {
    int insert(RequestLog requestLog);
}
