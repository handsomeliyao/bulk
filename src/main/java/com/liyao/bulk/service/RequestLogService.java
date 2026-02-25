package com.liyao.bulk.service;

import com.liyao.bulk.mapper.RequestLogMapper;
import com.liyao.bulk.model.RequestLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RequestLogService {

    private static final Logger log = LoggerFactory.getLogger(RequestLogService.class);

    private final RequestLogMapper requestLogMapper;

    public RequestLogService(RequestLogMapper requestLogMapper) {
        this.requestLogMapper = requestLogMapper;
    }

    @Async("taskExecutor")
    public void save(RequestLog requestLog) {
        try {
            requestLogMapper.insert(requestLog);
        } catch (Exception ex) {
            log.warn("Failed to save request log: {}", ex.getMessage(), ex);
        }
    }
}
