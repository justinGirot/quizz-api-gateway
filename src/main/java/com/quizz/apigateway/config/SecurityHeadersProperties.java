package com.quizz.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for security headers
 * Allows customization of security headers added to all responses
 */
@Component
@ConfigurationProperties(prefix = "security.headers")
public class SecurityHeadersProperties {

    /**
     * Enable or disable security headers
     */
    private boolean enabled = true;

    /**
     * X-Content-Type-Options header value
     * Prevents MIME type sniffing
     */
    private String contentTypeOptions = "nosniff";

    /**
     * X-Frame-Options header value
     * Prevents clickjacking attacks
     * Options: DENY, SAMEORIGIN, ALLOW-FROM uri
     */
    private String frameOptions = "DENY";

    /**
     * X-XSS-Protection header value
     * Enables XSS filter in browsers
     */
    private String xssProtection = "1; mode=block";

    /**
     * Strict-Transport-Security header value
     * Forces HTTPS connections (only applicable when using HTTPS)
     * Format: max-age=<seconds>; includeSubDomains; preload
     */
    private String strictTransportSecurity = "max-age=31536000; includeSubDomains";

    /**
     * Enable HSTS header (Strict-Transport-Security)
     * Only enable when using HTTPS in production
     */
    private boolean hstsEnabled = false;

    /**
     * Content-Security-Policy header value
     * Helps prevent XSS, clickjacking, and other code injection attacks
     */
    private String contentSecurityPolicy = "default-src 'self'";

    /**
     * Enable Content-Security-Policy header
     */
    private boolean cspEnabled = false;

    /**
     * Referrer-Policy header value
     * Controls how much referrer information is sent
     */
    private String referrerPolicy = "strict-origin-when-cross-origin";

    /**
     * Permissions-Policy header value (formerly Feature-Policy)
     * Controls which features and APIs can be used
     */
    private String permissionsPolicy = "geolocation=(), microphone=(), camera=()";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getContentTypeOptions() {
        return contentTypeOptions;
    }

    public void setContentTypeOptions(String contentTypeOptions) {
        this.contentTypeOptions = contentTypeOptions;
    }

    public String getFrameOptions() {
        return frameOptions;
    }

    public void setFrameOptions(String frameOptions) {
        this.frameOptions = frameOptions;
    }

    public String getXssProtection() {
        return xssProtection;
    }

    public void setXssProtection(String xssProtection) {
        this.xssProtection = xssProtection;
    }

    public String getStrictTransportSecurity() {
        return strictTransportSecurity;
    }

    public void setStrictTransportSecurity(String strictTransportSecurity) {
        this.strictTransportSecurity = strictTransportSecurity;
    }

    public boolean isHstsEnabled() {
        return hstsEnabled;
    }

    public void setHstsEnabled(boolean hstsEnabled) {
        this.hstsEnabled = hstsEnabled;
    }

    public String getContentSecurityPolicy() {
        return contentSecurityPolicy;
    }

    public void setContentSecurityPolicy(String contentSecurityPolicy) {
        this.contentSecurityPolicy = contentSecurityPolicy;
    }

    public boolean isCspEnabled() {
        return cspEnabled;
    }

    public void setCspEnabled(boolean cspEnabled) {
        this.cspEnabled = cspEnabled;
    }

    public String getReferrerPolicy() {
        return referrerPolicy;
    }

    public void setReferrerPolicy(String referrerPolicy) {
        this.referrerPolicy = referrerPolicy;
    }

    public String getPermissionsPolicy() {
        return permissionsPolicy;
    }

    public void setPermissionsPolicy(String permissionsPolicy) {
        this.permissionsPolicy = permissionsPolicy;
    }
}
