package com.liyao.bulk.model;

import lombok.Data;

@Data
public class SysMenu {
    private Long id;
    private Long pid;
    private String menuCode;
    private String menuName;
    private Integer menuOrder;
    private String icon;
    private String url;
}
