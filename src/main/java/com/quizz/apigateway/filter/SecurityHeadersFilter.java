package com.quizz.apigateway.filter;

import com.quizz.apigateway.config.SecurityHeadersProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter that adds security headers to all HTTP responses
 * Helps protect against common web vulnerabilities like XSS, clickjacking, and MIME sniffing
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityHeadersFilter.class);

    private final SecurityHeadersProperties properties;

    public SecurityHeadersFilter(SecurityHeadersProperties properties) {
        this.properties = properties;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (properties.isEnabled()) {
            logger.info("Security headers filter initialized");
            logger.debug("X-Content-Type-Options: {}", properties.getContentTypeOptions());
            logger.debug("X-Frame-Options: {}", properties.getFrameOptions());
            logger.debug("X-XSS-Protection: {}", properties.getXssProtection());
            logger.debug("Strict-Transport-Security enabled: {}", properties.isHstsEnabled());
        } else {
            logger.warn("Security headers filter is DISABLED");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (properties.isEnabled() && response instanceof HttpServletResponse httpResponse) {
            addSecurityHeaders(httpResponse);
        }

        chain.doFilter(request, response);
    }

    /**
     * Add security headers to the HTTP response
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        // Prevent MIME type sniffing
        // Helps prevent browsers from interpreting files as a different MIME type
        if (properties.getContentTypeOptions() != null) {
            response.setHeader("X-Content-Type-Options", properties.getContentTypeOptions());
        }

        // Prevent clickjacking attacks
        // Controls whether the page can be displayed in a frame/iframe
        if (properties.getFrameOptions() != null) {
            response.setHeader("X-Frame-Options", properties.getFrameOptions());
        }

        // Enable XSS protection in browsers
        // Activates browser's built-in XSS filter
        if (properties.getXssProtection() != null) {
            response.setHeader("X-XSS-Protection", properties.getXssProtection());
        }

        // Force HTTPS connections (only enable in production with HTTPS)
        // Tells browsers to only access the site via HTTPS
        if (properties.isHstsEnabled() && properties.getStrictTransportSecurity() != null) {
            response.setHeader("Strict-Transport-Security", properties.getStrictTransportSecurity());
        }

        // Content Security Policy (optional, more restrictive)
        // Helps prevent XSS, clickjacking, and other code injection attacks
        if (properties.isCspEnabled() && properties.getContentSecurityPolicy() != null) {
            response.setHeader("Content-Security-Policy", properties.getContentSecurityPolicy());
        }

        // Control referrer information
        // Determines how much referrer information is sent with requests
        if (properties.getReferrerPolicy() != null) {
            response.setHeader("Referrer-Policy", properties.getReferrerPolicy());
        }

        // Control browser features and APIs
        // Restricts access to browser features like geolocation, camera, microphone
        if (properties.getPermissionsPolicy() != null) {
            response.setHeader("Permissions-Policy", properties.getPermissionsPolicy());
        }
    }

    @Override
    public void destroy() {
        logger.debug("Security headers filter destroyed");
    }
}
