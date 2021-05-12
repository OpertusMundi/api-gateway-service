package eu.opertusmundi.web.controller.action;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.web.security.AuthenticationFacade;

public abstract class BaseController {

    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";

    @Autowired
    protected AuthenticationFacade authenticationFacade;

    protected boolean isAuthenticated() {
        return this.authenticationFacade.isAuthenticated();
    }

    protected Integer currentUserId() {
        return this.authenticationFacade.getCurrentUserId();
    }

    protected UUID currentUserKey() {
        return this.authenticationFacade.getCurrentUserKey();
    }

    protected String currentUserEmail() {
        return this.authenticationFacade.getCurrentUserEmail();
    }

    protected boolean hasRole(EnumRole role) {
        return this.authenticationFacade.hasRole(role);
    }

    protected void ensureRegistered() throws AccessDeniedException {
        if (!this.authenticationFacade.isRegistered()) {
            throw new AccessDeniedException("Access Denied");
        }
    }

    protected String getRemoteIpAddress() {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        final String result = Optional.ofNullable(request.getHeader(HEADER_X_FORWARDED_FOR)).orElse(request.getRemoteAddr());

        return result;
    }

}
