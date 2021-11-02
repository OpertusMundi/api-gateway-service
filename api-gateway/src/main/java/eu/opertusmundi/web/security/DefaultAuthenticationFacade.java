package eu.opertusmundi.web.security;

import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
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
    public boolean isRegistered() {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return false;
        }
        final User details = (User) authentication.getPrincipal();

        return details.getAccount().getActivationStatus() == EnumActivationStatus.COMPLETED;
    }

    @Override
    public AccountDto getCurrentAccount() {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((User) authentication.getPrincipal()).getAccount();
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
    public Integer getCurrentUserParentId() {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        final AccountDto account = ((User) authentication.getPrincipal()).getAccount();

        return account == null ? null : account.getParentId() == null ? account.getId() : account.getParentId();
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
    public UUID getCurrentUserParentKey() {
        final Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        final AccountDto account = ((User) authentication.getPrincipal()).getAccount();

        return account.getParentKey() == null ? account.getKey() : account.getParentKey();
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

    @Override
    public boolean hasAnyRole(EnumRole... roles) {
        if (roles == null) {
            return false;
        }
        for (final EnumRole role : roles) {
            if (this.hasRole(role)) {
                return true;
            }
        }
        return false;
    }

}