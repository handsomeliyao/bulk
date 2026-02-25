package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RequestLog {
    private Long id;
    private String requestId;
    private String method;
    private String path;
    private String queryString;
    private String handler;
    private String args;
    private Integer statusCode;
    private Boolean success;
    private String errorMessage;
    private Long costMs;
    private String clientIp;
    private String userAgent;
    private LocalDateTime createdAt;
}
