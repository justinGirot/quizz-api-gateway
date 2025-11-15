package com.quizz.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions.circuitBreaker;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

/**
 * Gateway route configuration with Circuit Breaker protection
 * Each route is protected by a circuit breaker with fallback endpoints
 */
@Configuration
public class GatewayConfig {

    /**
     * Auth Service route with circuit breaker
     * Fallback: /fallback/auth when service is unavailable
     */
    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth_service")
                .route(path("/api/auth/**"), http("lb://auth-service"))
                .filter(circuitBreaker("authServiceCircuitBreaker", "/fallback/auth"))
                .build();
    }

    /**
     * Question Service route with circuit breaker
     * Fallback: /fallback/questions when service is unavailable
     */
    @Bean
    public RouterFunction<ServerResponse> questionServiceRoute() {
        return route("question_service")
                .route(path("/api/questions/**"), http("lb://question-service"))
                .filter(circuitBreaker("questionServiceCircuitBreaker", "/fallback/questions"))
                .build();
    }

    /**
     * Quiz Service route with circuit breaker
     * Fallback: /fallback/quizzes when service is unavailable
     */
    @Bean
    public RouterFunction<ServerResponse> quizServiceRoute() {
        return route("quiz_service")
                .route(path("/api/quizzes/**"), http("lb://quiz-service"))
                .filter(circuitBreaker("quizServiceCircuitBreaker", "/fallback/quizzes"))
                .build();
    }
}

