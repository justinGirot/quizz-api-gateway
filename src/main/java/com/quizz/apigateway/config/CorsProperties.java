package com.quizz.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for CORS settings
 * Allows flexible configuration of CORS origins, headers, and methods
 */
@Data
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
}
