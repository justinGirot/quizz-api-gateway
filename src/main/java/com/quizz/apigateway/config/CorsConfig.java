package com.quizz.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration for the API Gateway
 * Configures allowed origins, headers, and methods for cross-origin requests
 */
@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(corsProperties.getAllowCredentials());

        // Configure allowed origins from properties
        // Use allowedOriginPatterns for development (supports wildcards like http://localhost:[*])
        // Use allowedOrigins for production (exact matches only)
        if (corsProperties.getAllowedOriginPatterns() != null && !corsProperties.getAllowedOriginPatterns().isEmpty()) {
            config.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
        } else if (corsProperties.getAllowedOrigins() != null && !corsProperties.getAllowedOrigins().isEmpty()) {
            config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        }

        // Explicitly define allowed headers (more secure than "*")
        config.setAllowedHeaders(List.of(
            "Authorization",           // JWT tokens, API keys
            "Content-Type",            // Request body type
            "Accept",                  // Response type preference
            "Origin",                  // CORS origin header
            "X-Requested-With",        // AJAX request identifier
            "X-Correlation-ID",        // Request tracing
            "Cache-Control",           // Caching directives
            "X-CSRF-Token"             // CSRF protection
        ));

        // Define allowed HTTP methods
        config.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS",
            "PATCH"
        ));

        // Expose headers that clients can access
        config.setExposedHeaders(List.of(
            "X-Correlation-ID",        // Allow client to read correlation ID
            "Content-Disposition"      // For file downloads
        ));

        // Cache preflight requests for the configured duration
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
