package com.quizz.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for request size limits and logging behavior
 * Helps prevent memory issues and DoS attacks from large requests
 */
@Component
@ConfigurationProperties(prefix = "gateway.request-limits")
public class RequestLimitProperties {

    /**
     * Maximum request size in bytes (default: 10MB)
     * Requests larger than this will be rejected with 413 Payload Too Large
     */
    private long maxRequestSize = 10 * 1024 * 1024; // 10MB

    /**
     * Maximum request size for body logging in bytes (default: 1MB)
     * Requests larger than this will only have metadata logged
     */
    private long maxLoggableSize = 1 * 1024 * 1024; // 1MB

    /**
     * Maximum size for caching request/response bodies (default: 1MB)
     * Bodies larger than this won't be cached in memory
     */
    private long maxCacheSize = 1 * 1024 * 1024; // 1MB

    /**
     * Content types that should always be cached/logged regardless of size
     * Useful for JSON APIs where you want to log all requests
     */
    private List<String> alwaysLogContentTypes = List.of(
        "application/json",
        "application/xml",
        "text/plain"
    );

    /**
     * Content types that should never be cached/logged
     * Useful for binary data like images, videos, files
     */
    private List<String> neverLogContentTypes = List.of(
        "multipart/form-data",
        "application/octet-stream",
        "image/*",
        "video/*",
        "audio/*"
    );

    /**
     * Enable request body caching for logging
     */
    private boolean enableBodyCaching = true;

    /**
     * Log request body content (only for small requests)
     */
    private boolean logRequestBody = false;

    /**
     * Log response body content (only for small responses)
     */
    private boolean logResponseBody = false;

    // Getters and Setters

    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public long getMaxLoggableSize() {
        return maxLoggableSize;
    }

    public void setMaxLoggableSize(long maxLoggableSize) {
        this.maxLoggableSize = maxLoggableSize;
    }

    public long getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public List<String> getAlwaysLogContentTypes() {
        return alwaysLogContentTypes;
    }

    public void setAlwaysLogContentTypes(List<String> alwaysLogContentTypes) {
        this.alwaysLogContentTypes = alwaysLogContentTypes;
    }

    public List<String> getNeverLogContentTypes() {
        return neverLogContentTypes;
    }

    public void setNeverLogContentTypes(List<String> neverLogContentTypes) {
        this.neverLogContentTypes = neverLogContentTypes;
    }

    public boolean isEnableBodyCaching() {
        return enableBodyCaching;
    }

    public void setEnableBodyCaching(boolean enableBodyCaching) {
        this.enableBodyCaching = enableBodyCaching;
    }

    public boolean isLogRequestBody() {
        return logRequestBody;
    }

    public void setLogRequestBody(boolean logRequestBody) {
        this.logRequestBody = logRequestBody;
    }

    public boolean isLogResponseBody() {
        return logResponseBody;
    }

    public void setLogResponseBody(boolean logResponseBody) {
        this.logResponseBody = logResponseBody;
    }
}
