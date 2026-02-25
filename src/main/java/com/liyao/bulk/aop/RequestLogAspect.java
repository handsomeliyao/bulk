package com.liyao.bulk.aop;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.model.RequestLog;
import com.liyao.bulk.service.RequestLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);
    private static final String UNKNOWN = "-";

    private final RequestLogService requestLogService;

    public RequestLogAspect(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    @Around("execution(* com.liyao.bulk..controller..*(..))")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        HttpServletRequest request = currentRequest();
        HttpServletResponse response = currentResponse();
        String uri = request == null ? UNKNOWN : request.getRequestURI();
        String method = request == null ? UNKNOWN : request.getMethod();
        String query = request == null ? "" : buildQuery(request.getParameterMap());
        String args = buildArgs(joinPoint.getArgs());
        String signature = joinPoint.getSignature().toShortString();
        String requestId = MDC.get("requestId");
        String clientIp = resolveClientIp(request);
        String userAgent = request == null ? null : request.getHeader("User-Agent");
        LocalDateTime startTime = LocalDateTime.now();
        RequestContext context = new RequestContext(
                start, startTime, method, uri, query, signature, requestId, args, clientIp, userAgent, response);

        log.info("REQ START {} {}{} | args={} | handler={}",
                method, uri, query, args, signature);
        try {
            Object result = joinPoint.proceed();
            StreamingResponseBody streamingBody = extractStreamingBody(result);
            if (streamingBody != null) {
                StreamingResponseBody wrapped = wrapStreaming(streamingBody, context);
                if (result instanceof ResponseEntity<?> responseEntity) {
                    return new ResponseEntity<>(
                            wrapped, responseEntity.getHeaders(), responseEntity.getStatusCode());
                }
                return wrapped;
            }

            long costMs = System.currentTimeMillis() - start;
            log.info("REQ END cost={}ms | handler={}", costMs, signature);
            writeLog(context, costMs, true, null, resolveStatus(response));
            return result;
        } catch (Exception ex) {
            long costMs = System.currentTimeMillis() - start;
            log.warn("REQ END cost={}ms | handler={} | error", costMs, signature, ex);
            writeLog(context, costMs, false, buildErrorMessage(ex), resolveStatus(response));
            if (ex instanceof BusinessException) {
                throw ex;
            }
            throw new IllegalStateException(String.format("request failed for %s %s%s | handler=%s", method, uri, query, signature), ex);
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private String buildQuery(Map<String, String[]> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + Arrays.toString(e.getValue()))
                .collect(Collectors.joining("&"));
        return "?" + query;
    }

    private String buildArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest))
                .filter(arg -> !(arg instanceof HttpServletResponse))
                .filter(arg -> !(arg instanceof MultipartFile))
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private StreamingResponseBody extractStreamingBody(Object result) {
        if (result instanceof StreamingResponseBody streamingBody) {
            return streamingBody;
        }
        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            if (body instanceof StreamingResponseBody streamingBody) {
                return streamingBody;
            }
        }
        return null;
    }

    private StreamingResponseBody wrapStreaming(
            StreamingResponseBody delegate,
            RequestContext context
    ) {
        return outputStream -> {
            String previous = MDC.get("requestId");
            if (context.requestId != null) {
                MDC.put("requestId", context.requestId);
            } else {
                MDC.remove("requestId");
            }
            try {
                delegate.writeTo(outputStream);
                long costMs = System.currentTimeMillis() - context.start;
                log.info("REQ END cost={}ms | handler={}", costMs, context.signature);
                writeLog(context, costMs, true, null, resolveStatus(context.response));
            } catch (Exception ex) {
                long costMs = System.currentTimeMillis() - context.start;
                log.warn("REQ END cost={}ms | handler={} | error",
                        costMs, context.signature, ex);
                writeLog(context, costMs, false, buildErrorMessage(ex), resolveStatus(context.response));
                if (ex instanceof BusinessException) {
                    throw ex;
                }
                throw new IllegalStateException(
                        String.format("streaming failed for %s %s%s | handler=%s",
                                context.method, context.uri, context.query, context.signature), ex);
            } finally {
                if (previous != null) {
                    MDC.put("requestId", previous);
                } else {
                    MDC.remove("requestId");
                }
            }
        };
    }

    private record RequestContext(
            long start,
            LocalDateTime startTime,
            String method,
            String uri,
            String query,
            String signature,
            String requestId,
            String args,
            String clientIp,
            String userAgent,
            HttpServletResponse response
    ) {
    }

    private void writeLog(RequestContext context, long costMs, boolean success, String error, Integer status) {
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestId(context.requestId);
        requestLog.setMethod(context.method);
        requestLog.setPath(context.uri);
        requestLog.setQueryString(context.query);
        requestLog.setHandler(context.signature);
        requestLog.setArgs(context.args);
        requestLog.setStatusCode(status);
        requestLog.setSuccess(success);
        requestLog.setErrorMessage(error);
        requestLog.setCostMs(costMs);
        requestLog.setClientIp(context.clientIp);
        requestLog.setUserAgent(context.userAgent);
        requestLog.setCreatedAt(context.startTime);
        requestLogService.save(requestLog);
    }

    private Integer resolveStatus(HttpServletResponse response) {
        return response == null ? null : response.getStatus();
    }

    private HttpServletResponse currentResponse() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getResponse();
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private String buildErrorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return ex.getClass().getSimpleName() + ": " + message;
    }
}
