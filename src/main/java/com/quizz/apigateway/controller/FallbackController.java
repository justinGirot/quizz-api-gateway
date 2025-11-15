package com.quizz.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller that provides graceful error responses when backend services are unavailable
 * Invoked by Circuit Breaker when services are down or experiencing issues
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Fallback for Auth Service
     * Returns a service unavailable response with helpful information
     */
    @GetMapping("/auth")
    @PostMapping("/auth")
    @PutMapping("/auth")
    @DeleteMapping("/auth")
    @PatchMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        log.warn("Auth service is currently unavailable - circuit breaker activated");
        return createFallbackResponse(
            "Auth Service",
            "The authentication service is temporarily unavailable. Please try again in a few moments."
        );
    }

    /**
     * Fallback for Question Service
     */
    @GetMapping("/questions")
    @PostMapping("/questions")
    @PutMapping("/questions")
    @DeleteMapping("/questions")
    @PatchMapping("/questions")
    public ResponseEntity<Map<String, Object>> questionServiceFallback() {
        log.warn("Question service is currently unavailable - circuit breaker activated");
        return createFallbackResponse(
            "Question Service",
            "The question service is temporarily unavailable. Please try again in a few moments."
        );
    }

    /**
     * Fallback for Quiz Service
     */
    @GetMapping("/quizzes")
    @PostMapping("/quizzes")
    @PutMapping("/quizzes")
    @DeleteMapping("/quizzes")
    @PatchMapping("/quizzes")
    public ResponseEntity<Map<String, Object>> quizServiceFallback() {
        log.warn("Quiz service is currently unavailable - circuit breaker activated");
        return createFallbackResponse(
            "Quiz Service",
            "The quiz service is temporarily unavailable. Please try again in a few moments."
        );
    }

    /**
     * Generic fallback for any unhandled service
     */
    @GetMapping("/default")
    @PostMapping("/default")
    @PutMapping("/default")
    @DeleteMapping("/default")
    @PatchMapping("/default")
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        log.warn("Service is currently unavailable - circuit breaker activated");
        return createFallbackResponse(
            "Service",
            "The requested service is temporarily unavailable. Please try again in a few moments."
        );
    }

    /**
     * Creates a standardized fallback response
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("service", serviceName);
        response.put("circuitBreakerActive", true);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
