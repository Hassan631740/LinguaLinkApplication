package com.lingualink.security;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;

import java.security.Principal;

/**
 * Utility class for working with Spring Security authentication in WebSocket contexts.
 */
public class WebSocketSecurityUtils {

    /**
     * Extracts Authentication from SimpMessageHeaderAccessor.
     * 
     * @param headerAccessor the message header accessor
     * @return Authentication object, or null if not found
     */
    public static Authentication getAuthentication(SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal instanceof Authentication) {
            return (Authentication) principal;
        }
        return null;
    }

    /**
     * Gets the username from the WebSocket session.
     * 
     * @param headerAccessor the message header accessor
     * @return username, or null if not available
     */
    public static String getUsername(SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            return principal.getName();
        }
        return null;
    }

    /**
     * Gets the username from a Principal object.
     * 
     * @param principal the principal
     * @return username, or null if principal is null
     */
    public static String getUsername(Principal principal) {
        if (principal != null) {
            return principal.getName();
        }
        return null;
    }
}

