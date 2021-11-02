package eu.opertusmundi.web.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.account.AccountDto;

public interface AuthenticationFacade {

    /**
     * Get the current {@link Authentication} object from the
     * {@link SecurityContextHolder}
     *
     * @return An instance of {@link Authentication}
     */
    Authentication getAuthentication();

    /**
     * Get user authentication status
     *
     * @return True if there is an authenticated user for the current session;
     *         Otherwise, False is returned
     */
    boolean isAuthenticated();

    /**
     * Get user registration status
     *
     * @return True if the user is authenticated and the email address has been
     *         verified
     */
    boolean isRegistered();

    /**
     * Get the current account
     *
     * @return the current account or {@code null} if the user is not authenticated
     */
    AccountDto getCurrentAccount();

    /**
     * Get the user id
     *
     * @return the user id or {@code null} if the user is not authenticated
     */
    Integer getCurrentUserId();

    /**
     * Get the user parent id
     *
     * @return the user parent id or the user id if parent (vendor) does not
     *         exist. If the user is not authenticated, {@code null} is
     *         returned.
     */
    Integer getCurrentUserParentId();

    /**
     * Get the user key
     *
     * @return the user key or {@code null} if the user is not authenticated
     */
    UUID getCurrentUserKey();

    /**
     * Get the user parent key
     *
     * @return the user parent key or user key if there is not parent. If the
     *         user is not authenticated, {@code null} is returned
     */
    UUID getCurrentUserParentKey();

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
     * Check if current user has any of the specified roles
     *
     * @param roles
     * @return
     */
    boolean hasAnyRole(EnumRole... roles);

    /**
     * Get all roles of the current user
     *
     * @return
     */
    EnumRole[] getRoles();

}