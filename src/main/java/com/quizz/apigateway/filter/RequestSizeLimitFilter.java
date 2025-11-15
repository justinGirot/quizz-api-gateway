package com.quizz.apigateway.filter;

import com.quizz.apigateway.config.RequestLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to enforce request size limits
 * Prevents memory exhaustion and DoS attacks from excessively large requests
 * Runs early in the filter chain (HIGHEST_PRECEDENCE + 10)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestSizeLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestSizeLimitFilter.class);

    private final RequestLimitProperties requestLimitProperties;

    public RequestSizeLimitFilter(RequestLimitProperties requestLimitProperties) {
        this.requestLimitProperties = requestLimitProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long contentLength = request.getContentLengthLong();

        // Check if content length exceeds maximum
        if (contentLength > requestLimitProperties.getMaxRequestSize()) {
            logger.warn("Request rejected - size {} bytes exceeds limit of {} bytes. URI: {}, Method: {}, RemoteAddr: {}",
                    contentLength,
                    requestLimitProperties.getMaxRequestSize(),
                    request.getRequestURI(),
                    request.getMethod(),
                    request.getRemoteAddr());

            // Return 413 Payload Too Large
            response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"timestamp\":\"%s\",\"status\":413,\"error\":\"Payload Too Large\"," +
                    "\"message\":\"Request size %d bytes exceeds maximum allowed size of %d bytes\"," +
                    "\"path\":\"%s\"}",
                    java.time.LocalDateTime.now().toString(),
                    contentLength,
                    requestLimitProperties.getMaxRequestSize(),
                    request.getRequestURI()
            ));
            return;
        }

        // Log warning for large (but acceptable) requests
        if (contentLength > requestLimitProperties.getMaxLoggableSize()) {
            logger.info("Large request detected - size {} bytes. URI: {}, Method: {}",
                    contentLength,
                    request.getRequestURI(),
                    request.getMethod());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip for actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}
