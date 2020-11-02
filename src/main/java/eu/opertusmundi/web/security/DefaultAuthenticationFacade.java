package eu.opertusmundi.web.security;

import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.web.model.security.User;


@Component
public class DefaultAuthenticationFacade implements AuthenticationFacade {

    @Override
    public Authentication getAuthentication() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication;
    }

    @Override
    public boolean isAuthenticated() {
        return this.getCurrentUserId() != null;
    }

    @Override
    public Integer getCurrentUserId() {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((User) authentication.getPrincipal()).getId();
    }

    @Override
    public UUID getCurrentUserKey() {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((User) authentication.getPrincipal()).getAccount().getKey();
    }

    @Override
    public String getCurrentUserEmail() {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((User) authentication.getPrincipal()).getEmail();
    }

    @Override
    public EnumRole[] getRoles() {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return new EnumRole[] {};
        }
        return authentication.getAuthorities().stream()
            .map(a -> EnumRole.fromString(a.getAuthority()))
            .toArray(EnumRole[]::new);
    }


    @Override
    public boolean hasRole(EnumRole role) {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return false;
        }
        return ((User) authentication.getPrincipal()).hasRole(role);
    }

}