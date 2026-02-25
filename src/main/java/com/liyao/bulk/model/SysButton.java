package com.liyao.bulk.model;

import lombok.Data;

@Data
public class SysButton {
    private Long id;
    private Long menuId;
    private String btnCode;
    private String btnName;
    private String method;
    private String uri;
}
