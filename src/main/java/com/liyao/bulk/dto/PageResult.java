package com.liyao.bulk.dto;

import java.util.List;
import lombok.Data;

@Data
public class PageResult<T> {
    private long total;
    private int pageNum;
    private int pageSize;
    private List<T> list;
}
