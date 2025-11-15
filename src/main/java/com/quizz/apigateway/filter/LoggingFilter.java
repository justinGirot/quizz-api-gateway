package com.quizz.apigateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * Logging filter for tracking requests and responses through the gateway
 * Adds correlation ID to each request for distributed tracing
 */
@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Generate or use existing correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add correlation ID to response
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // Wrap request and response for content reading
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            logRequest(wrappedRequest, correlationId);

            // Continue filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log outgoing response
            logResponse(wrappedResponse, correlationId, duration);

            // Copy response body back to original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request, String correlationId) {
        log.info("Incoming Request [{}] - Method: {}, URI: {}, RemoteAddr: {}",
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        // Log query string if present
        if (request.getQueryString() != null) {
            log.debug("Query String [{}]: {}", correlationId, request.getQueryString());
        }

        // Log authorization header (masked)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            log.debug("Authorization [{}]: {}", correlationId, maskAuthHeader(authHeader));
        }
    }

    private void logResponse(HttpServletResponse response, String correlationId, long duration) {
        log.info("Outgoing Response [{}] - Status: {}, Duration: {}ms",
                correlationId,
                response.getStatus(),
                duration);
    }

    private String maskAuthHeader(String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (token.length() > 10) {
                return "Bearer " + token.substring(0, 10) + "..." + token.substring(token.length() - 4);
            }
        }
        return "Bearer ***";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip logging for actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}
