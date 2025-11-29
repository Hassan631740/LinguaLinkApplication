package com.lingualink.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

/**
 * Handshake interceptor that validates JWT tokens during WebSocket connection.
 * JWT token can be provided either:
 * 1. As a query parameter: ?token=JWT_TOKEN
 * 2. In the Authorization header: Authorization: Bearer JWT_TOKEN
 */
@Slf4j
@Component
public class JwtWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private static final String TOKEN_PARAM = "token";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtWebSocketHandshakeInterceptor(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            
            // Try to extract JWT token from query parameter or header
            String token = extractToken(httpRequest);
            
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket handshake rejected: No JWT token provided. URI: {}", request.getURI());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            
            try {
                // Decode and validate JWT token
                Jwt jwt = jwtDecoder.decode(token);
                
                // Convert JWT to Authentication object
                Authentication authentication = jwtAuthenticationConverter.convert(jwt);
                
                if (authentication == null) {
                    log.warn("WebSocket handshake rejected: Failed to convert JWT to authentication. URI: {}", request.getURI());
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }
                
                // Store authentication in attributes for WebSocket session
                // Spring Security will use this to set up the Principal in message handlers
                attributes.put("SPRING_SECURITY_CONTEXT_AUTHENTICATION", authentication);
                
                // Also store JWT and username for easy access
                attributes.put("jwt", jwt);
                attributes.put("username", jwt.getSubject());
                
                log.info("WebSocket handshake accepted for user: {}", jwt.getSubject());
                return true;
                
            } catch (JwtException e) {
                log.warn("WebSocket handshake rejected: Invalid JWT token. URI: {}, Error: {}", request.getURI(), e.getMessage());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            } catch (Exception e) {
                log.error("WebSocket handshake error: {}", e.getMessage(), e);
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return false;
            }
        }
        
        log.warn("WebSocket handshake rejected: Invalid request type. URI: {}", request.getURI());
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Can be used for cleanup or logging after handshake completes
        if (exception != null) {
            log.error("WebSocket handshake exception: {}", exception.getMessage(), exception);
        }
    }

    /**
     * Extracts JWT token from request.
     * Checks query parameter first, then Authorization header.
     */
    private String extractToken(HttpServletRequest request) {
        // Try query parameter first (common for WebSocket connections)
        String token = request.getParameter(TOKEN_PARAM);
        if (token != null && !token.isEmpty()) {
            return token;
        }
        
        // Try Authorization header
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
}

