package com.quizz.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for CORS settings
 * Allows flexible configuration of CORS origins, headers, and methods
 */
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * List of allowed origins (exact matches)
     * Example: http://localhost:5174, https://myapp.com
     */
    private List<String> allowedOrigins = List.of("http://localhost:5174");

    /**
     * List of allowed origin patterns (supports wildcards)
     * Example: http://localhost:*, https://*.myapp.com
     * Use this for development environments for more flexible matching
     */
    private List<String> allowedOriginPatterns = List.of("http://localhost:[*]");

    /**
     * Maximum age (in seconds) for CORS preflight cache
     */
    private Long maxAge = 3600L;

    /**
     * Whether to allow credentials (cookies, authorization headers)
     */
    private Boolean allowCredentials = true;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }

    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }
}
