package com.quizz.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Routes can be configured via properties to use either:
 * - Service Discovery (Eureka): lb://service-name
 * - Direct URLs: http://host:port
 *
 * All routing parameters are externalized to application.yml via RouteProperties
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final RouteProperties routeProperties;

    /**
     * Auth Service route with circuit breaker
     * Target URI and all parameters configured via properties
     */
    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        RouteProperties.ServiceRoute config = routeProperties.getAuthService();
        String targetUri = config.getTargetUri(routeProperties.isUseServiceDiscovery());

        log.info("Configuring Auth Service route: {} -> {}", config.getPath(), targetUri);

        return route("auth_service")
                .route(path(config.getPath()), http(targetUri))
                .filter(circuitBreaker(config.getCircuitBreakerName(), config.getFallbackPath()))
                .build();
    }

    /**
     * Question Service route with circuit breaker
     * Target URI and all parameters configured via properties
     */
    @Bean
    public RouterFunction<ServerResponse> questionServiceRoute() {
        RouteProperties.ServiceRoute config = routeProperties.getQuestionService();
        String targetUri = config.getTargetUri(routeProperties.isUseServiceDiscovery());

        log.info("Configuring Question Service route: {} -> {}", config.getPath(), targetUri);

        return route("question_service")
                .route(path(config.getPath()), http(targetUri))
                .filter(circuitBreaker(config.getCircuitBreakerName(), config.getFallbackPath()))
                .build();
    }

    /**
     * Category Service route with circuit breaker
     * Routes to question-service as categories are managed there
     * Target URI and all parameters configured via properties
     */
    @Bean
    public RouterFunction<ServerResponse> categoryServiceRoute() {
        RouteProperties.ServiceRoute config = routeProperties.getCategoryService();
        String targetUri = config.getTargetUri(routeProperties.isUseServiceDiscovery());

        log.info("Configuring Category Service route: {} -> {}", config.getPath(), targetUri);

        return route("category_service")
                .route(path(config.getPath()), http(targetUri))
                .filter(circuitBreaker(config.getCircuitBreakerName(), config.getFallbackPath()))
                .build();
    }

    /**
     * Quiz Service route with circuit breaker
     * Target URI and all parameters configured via properties
     */
    @Bean
    public RouterFunction<ServerResponse> quizServiceRoute() {
        RouteProperties.ServiceRoute config = routeProperties.getQuizService();
        String targetUri = config.getTargetUri(routeProperties.isUseServiceDiscovery());

        log.info("Configuring Quiz Service route: {} -> {}", config.getPath(), targetUri);

        return route("quiz_service")
                .route(path(config.getPath()), http(targetUri))
                .filter(circuitBreaker(config.getCircuitBreakerName(), config.getFallbackPath()))
                .build();
    }
}

