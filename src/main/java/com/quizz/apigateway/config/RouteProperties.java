package com.quizz.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for gateway routing
 * Allows flexible configuration for service discovery (Eureka) or direct URL routing
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.routes")
public class RouteProperties {

    /**
     * Enable service discovery (Eureka) for routing
     * When true: uses lb://service-name (load balanced via Eureka)
     * When false: uses direct service URLs
     */
    private boolean useServiceDiscovery = true;

    /**
     * Auth Service route configuration
     */
    private ServiceRoute authService = new ServiceRoute(
            "/api/auth/**",
            "http://localhost:8081",
            "auth-service",
            "authServiceCircuitBreaker",
            "/fallback/auth"
    );

    /**
     * Question Service route configuration
     */
    private ServiceRoute questionService = new ServiceRoute(
            "/api/questions/**",
            "http://localhost:8082",
            "question-service",
            "questionServiceCircuitBreaker",
            "/fallback/questions"
    );

    /**
     * Quiz Service route configuration
     */
    private ServiceRoute quizService = new ServiceRoute(
            "/api/quizzes/**",
            "http://localhost:8083",
            "quiz-service",
            "quizServiceCircuitBreaker",
            "/fallback/quizzes"
    );

    /**
     * Individual service route configuration
     */
    @Data
    public static class ServiceRoute {
        /**
         * Path pattern for routing (e.g., /api/auth/**)
         */
        private String path;

        /**
         * Direct service URL (used when useServiceDiscovery = false)
         * Example: http://localhost:8081
         */
        private String serviceUrl;

        /**
         * Service name for Eureka discovery (used when useServiceDiscovery = true)
         * Example: auth-service
         */
        private String serviceName;

        /**
         * Circuit breaker instance name
         * Must match Resilience4j configuration in application.yml
         */
        private String circuitBreakerName;

        /**
         * Fallback endpoint path when circuit breaker opens
         * Example: /fallback/auth
         */
        private String fallbackPath;

        public ServiceRoute() {
        }

        public ServiceRoute(String path, String serviceUrl, String serviceName,
                            String circuitBreakerName, String fallbackPath) {
            this.path = path;
            this.serviceUrl = serviceUrl;
            this.serviceName = serviceName;
            this.circuitBreakerName = circuitBreakerName;
            this.fallbackPath = fallbackPath;
        }

        /**
         * Get the target URI based on service discovery settings
         * @param useServiceDiscovery whether to use Eureka service discovery
         * @return URI string (either lb://service-name or http://host:port)
         */
        public String getTargetUri(boolean useServiceDiscovery) {
            return useServiceDiscovery ? "lb://" + serviceName : serviceUrl;
        }
    }
}
