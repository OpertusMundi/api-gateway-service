package eu.opertusmundi.web.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.opertusmundi.common.model.EnumRole;

public interface AuthenticationFacade {

    /**
     * Get the current {@link Authentication} object from the {@link SecurityContextHolder}
     * @return An instance of {@link Authentication}
     */
    Authentication getAuthentication();

    /**
     * Get user authentication status
     * @return True if there is an authenticated user for the current session; Otherwise, False is returned
     */
    boolean isAuthenticated();

    /**
     * Get the user id
     *
     * @return the user id or {@code null} if the user is not authenticated
     */
    Integer getCurrentUserId();

    /**
     * Get the user key
     *
     * @return the user id or {@code null} if the user is not authenticated
     */
    UUID getCurrentUserKey();

    /**
     * Get the user email
     *
     * @return the user email or {@code null} if the user is not authenticated
     */
    String getCurrentUserEmail();

    /**
     * Check if current user has the specified role
     *
     * @param role The role to check
     *
     * @return True if the user has the role
     */
    boolean hasRole(EnumRole role);

    /**
     * Get all roles of the current user
     *
     * @return
     */
    EnumRole[] getRoles();

}