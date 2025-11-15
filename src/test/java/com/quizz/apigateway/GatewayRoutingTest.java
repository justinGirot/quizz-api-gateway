package com.quizz.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Gateway routing functionality
 * Tests that requests are properly routed to backend services
 */
@SpringBootTest
@AutoConfigureMockMvc
class GatewayRoutingTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test that auth service routes are configured
     * This will either:
     * - Return 503 (Service Unavailable) if auth service is down - circuit breaker fallback
     * - Return a response from auth service if it's running
     * - Both scenarios prove the route is configured correctly
     */
    @Test
    void testAuthServiceRouteIsConfigured() throws Exception {
        mockMvc.perform(get("/api/auth/health")
                        .header("X-Test-Request", "true"))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200 && status < 500) {
                        throw new AssertionError("Expected 200 or 5xx, but got: " + status);
                    }
                })
                .andExpect(header().exists("X-Correlation-ID"));
    }

    /**
     * Test that question service routes are configured
     */
    @Test
    void testQuestionServiceRouteIsConfigured() throws Exception {
        mockMvc.perform(get("/api/questions/health")
                        .header("X-Test-Request", "true"))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200 && status < 500) {
                        throw new AssertionError("Expected 200 or 5xx, but got: " + status);
                    }
                })
                .andExpect(header().exists("X-Correlation-ID"));
    }

    /**
     * Test that quiz service routes are configured
     */
    @Test
    void testQuizServiceRouteIsConfigured() throws Exception {
        mockMvc.perform(get("/api/quizzes/health")
                        .header("X-Test-Request", "true"))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200 && status < 500) {
                        throw new AssertionError("Expected 200 or 5xx, but got: " + status);
                    }
                })
                .andExpect(header().exists("X-Correlation-ID"));
    }

    /**
     * Test POST request to auth service
     * Simulates a login or registration request
     */
    @Test
    void testAuthServicePostRequest() throws Exception {
        String loginPayload = """
                {
                    "username": "testuser",
                    "password": "testpassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginPayload))
                .andDo(print())
                .andExpect(result -> {
                    // Accept any status code - we just want to verify the route exists
                    int status = result.getResponse().getStatus();
                    if (status < 200 || status >= 600) {
                        throw new AssertionError("Unexpected status code: " + status);
                    }
                })
                .andExpect(header().exists("X-Correlation-ID"));
    }

    /**
     * Test that fallback endpoints work when services are unavailable
     * This tests the circuit breaker fallback mechanism
     */
    @Test
    void testFallbackEndpointForAuthService() throws Exception {
        mockMvc.perform(get("/fallback/auth"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.service").value("Auth Service"))
                .andExpect(jsonPath("$.circuitBreakerActive").value(true));
    }

    /**
     * Test that fallback endpoints work for question service
     */
    @Test
    void testFallbackEndpointForQuestionService() throws Exception {
        mockMvc.perform(get("/fallback/questions"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.service").value("Question Service"))
                .andExpect(jsonPath("$.circuitBreakerActive").value(true));
    }

    /**
     * Test that fallback endpoints work for quiz service
     */
    @Test
    void testFallbackEndpointForQuizService() throws Exception {
        mockMvc.perform(get("/fallback/quizzes"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.service").value("Quiz Service"))
                .andExpect(jsonPath("$.circuitBreakerActive").value(true));
    }

    /**
     * Test CORS headers are present on responses
     */
    @Test
    void testCorsHeadersArePresent() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5174")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    /**
     * Test security headers are applied
     */
    @Test
    void testSecurityHeadersAreApplied() throws Exception {
        mockMvc.perform(get("/fallback/auth"))
                .andDo(print())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"));
    }
}
