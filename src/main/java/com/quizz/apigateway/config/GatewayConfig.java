package com.quizz.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth_service")
                .route(path("/api/auth/**"), http("lb://auth-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> questionServiceRoute() {
        return route("question_service")
                .route(path("/api/questions/**"), http("lb://question-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> quizServiceRoute() {
        return route("quiz_service")
                .route(path("/api/quizzes/**"), http("lb://quiz-service"))
                .build();
    }
}
