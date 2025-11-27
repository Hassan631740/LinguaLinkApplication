package com.lingualink.security;

import com.lingualink.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class SecurityUtils {
    
    /**
     * Check if the provided user ID matches the current authenticated user's ID
     * This method is used in SpEL expressions for method security
     */
    public boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }
    
    /**
     * Get the current authenticated user's ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        return null;
    }

    /**
     * Get the current authenticated user's email
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(Role role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = "ROLE_" + role.getValue();
        
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleName));
    }

    /**
     * Check if the current user is an administrator
     */
    public static boolean isAdministrator() {
        return hasRole(Role.ADMINISTRATOR);
    }

    /**
     * Check if the current user is an interpreter
     */
    public static boolean isInterpreter() {
        return hasRole(Role.INTERPRETER);
    }

    /**
     * Check if the current user is a client
     */
    public static boolean isClient() {
        return hasRole(Role.CLIENT);
    }

    /**
     * Check if the current user is an administrator or has a specific role
     */
    public static boolean isAdministratorOrHasRole(Role role) {
        return isAdministrator() || hasRole(role);
    }

    /**
     * Get the current user's role
     */
    public static Role getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            if (authorityName.startsWith("ROLE_")) {
                String roleName = authorityName.substring(5); // Remove "ROLE_" prefix
                return Role.fromString(roleName);
            }
        }
        return Role.CLIENT; // Default
    }
}

