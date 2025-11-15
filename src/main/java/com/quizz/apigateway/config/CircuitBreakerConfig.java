package com.quizz.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit Breaker configuration using Resilience4j
 * Provides resilience patterns for gateway routes to handle service failures gracefully
 */
@Configuration
public class CircuitBreakerConfig {

    /**
     * Configure default circuit breaker settings
     * These settings apply to all circuit breakers unless overridden
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                        // Percentage of failed calls to open the circuit
                        .failureRateThreshold(50.0f)

                        // Percentage of slow calls to open the circuit
                        .slowCallRateThreshold(50.0f)

                        // Duration threshold for slow calls (2 seconds)
                        .slowCallDurationThreshold(Duration.ofSeconds(2))

                        // Number of calls in closed state before calculating error rate
                        .slidingWindowSize(10)

                        // Type of sliding window (count-based)
                        .slidingWindowType(SlidingWindowType.COUNT_BASED)

                        // Minimum number of calls before circuit breaker can trip
                        .minimumNumberOfCalls(5)

                        // Time the circuit stays open before transitioning to half-open
                        .waitDurationInOpenState(Duration.ofSeconds(30))

                        // Number of permitted calls in half-open state
                        .permittedNumberOfCallsInHalfOpenState(3)

                        // Automatically transition from open to half-open
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)

                        // Record these exceptions as failures
                        .recordExceptions(
                                java.io.IOException.class,
                                java.util.concurrent.TimeoutException.class,
                                org.springframework.web.client.ResourceAccessException.class
                        )

                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        // Maximum time to wait for a response (3 seconds)
                        .timeoutDuration(Duration.ofSeconds(3))
                        .build())
                .build());
    }

    /**
     * Expose CircuitBreakerRegistry for monitoring and metrics
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}
