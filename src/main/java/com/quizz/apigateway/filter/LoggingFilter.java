package com.quizz.apigateway.filter;

import com.quizz.apigateway.config.RequestLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Logging filter for tracking requests and responses through the gateway
 * Adds correlation ID to each request for distributed tracing
 * Implements memory-safe logging with configurable size limits
 */
@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private final RequestLimitProperties requestLimitProperties;

    public LoggingFilter(RequestLimitProperties requestLimitProperties) {
        this.requestLimitProperties = requestLimitProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Generate or use existing correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add correlation ID to response
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        long contentLength = request.getContentLengthLong();
        boolean shouldCache = shouldCacheContent(request, contentLength);

        HttpServletRequest requestToUse = request;
        HttpServletResponse responseToUse = response;

        // Only wrap for caching if content is small enough and caching is enabled
        if (shouldCache && requestLimitProperties.isEnableBodyCaching()) {
            requestToUse = new ContentCachingRequestWrapper(request);
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request (metadata only for large requests)
            logRequest(requestToUse, correlationId, contentLength);

            // Continue filter chain
            filterChain.doFilter(requestToUse, responseToUse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log outgoing response
            logResponse(responseToUse, correlationId, duration);

            // Copy response body back if it was cached
            if (responseToUse instanceof ContentCachingResponseWrapper) {
                ((ContentCachingResponseWrapper) responseToUse).copyBodyToResponse();
            }
        }
    }

    /**
     * Determine if request/response content should be cached in memory
     * Based on size limits and content type
     */
    private boolean shouldCacheContent(HttpServletRequest request, long contentLength) {
        // Don't cache if content is too large
        if (contentLength > requestLimitProperties.getMaxCacheSize()) {
            log.debug("Skipping content caching - size {} exceeds max cache size {}",
                    contentLength, requestLimitProperties.getMaxCacheSize());
            return false;
        }

        String contentType = request.getContentType();
        if (contentType == null) {
            return true; // Cache if no content type specified
        }

        // Never cache these content types
        for (String neverLog : requestLimitProperties.getNeverLogContentTypes()) {
            if (contentType.toLowerCase().startsWith(neverLog.replace("*", "").toLowerCase())) {
                log.debug("Skipping content caching - content type {} is in never-log list", contentType);
                return false;
            }
        }

        return true;
    }

    private void logRequest(HttpServletRequest request, String correlationId, long contentLength) {
        String contentType = request.getContentType();

        // Always log request metadata
        log.info("Incoming Request [{}] - Method: {}, URI: {}, RemoteAddr: {}, ContentType: {}, Size: {} bytes",
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                contentType != null ? contentType : "none",
                contentLength > 0 ? contentLength : "unknown");

        // Log query string if present
        if (request.getQueryString() != null) {
            log.debug("Query String [{}]: {}", correlationId, request.getQueryString());
        }

        // Log authorization header (masked)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            log.debug("Authorization [{}]: {}", correlationId, maskAuthHeader(authHeader));
        }

        // Log request body only for small requests and if enabled
        if (requestLimitProperties.isLogRequestBody() &&
            request instanceof ContentCachingRequestWrapper &&
            contentLength > 0 && contentLength <= requestLimitProperties.getMaxLoggableSize()) {

            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] content = wrapper.getContentAsByteArray();

            if (content.length > 0 && content.length <= requestLimitProperties.getMaxLoggableSize()) {
                String body = new String(content, StandardCharsets.UTF_8);
                log.debug("Request Body [{}]: {}", correlationId, body);
            }
        } else if (contentLength > requestLimitProperties.getMaxLoggableSize()) {
            log.debug("Request Body [{}]: <large request - body not logged, size: {} bytes>",
                    correlationId, contentLength);
        }
    }

    private void logResponse(HttpServletResponse response, String correlationId, long duration) {
        log.info("Outgoing Response [{}] - Status: {}, Duration: {}ms",
                correlationId,
                response.getStatus(),
                duration);

        // Log response body only if enabled and response is small
        if (requestLimitProperties.isLogResponseBody() && response instanceof ContentCachingResponseWrapper) {
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
            byte[] content = wrapper.getContentAsByteArray();

            if (content.length > 0 && content.length <= requestLimitProperties.getMaxLoggableSize()) {
                String body = new String(content, StandardCharsets.UTF_8);
                log.debug("Response Body [{}]: {}", correlationId, body);
            } else if (content.length > requestLimitProperties.getMaxLoggableSize()) {
                log.debug("Response Body [{}]: <large response - body not logged, size: {} bytes>",
                        correlationId, content.length);
            }
        }
    }

    private String maskAuthHeader(String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (token.length() > 10) {
                return "Bearer " + token.substring(0, 10) + "..." + token.substring(token.length() - 4);
            }
        }
        return "Bearer ***";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip logging for actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}
