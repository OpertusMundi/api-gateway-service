package eu.opertusmundi.web.controller.action;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.RequestContext;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.location.Location;
import eu.opertusmundi.common.service.GeoIpApiLocationService;
import eu.opertusmundi.common.service.LocationService;
import eu.opertusmundi.web.model.Constants;
import eu.opertusmundi.web.security.AuthenticationFacade;

public abstract class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(GeoIpApiLocationService.class);

    @Value("#{environment.getActiveProfiles()[0] == 'development' ? '${opertusmundi.debug.remote-ip-address:}' : ''}")
    private String fixedRemoteIpAddress;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired(required = false)
    private LocationService locationService;

    protected boolean isAuthenticated() {
        return this.authenticationFacade.isAuthenticated();
    }

    protected AccountDto getAccount() {
        return this.authenticationFacade.getCurrentAccount();
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
        if (!StringUtils.isBlank(fixedRemoteIpAddress)) {
            return fixedRemoteIpAddress;
        }

        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        final String result = Optional.ofNullable(request.getHeader(Constants.HEADER_X_FORWARDED_FOR)).orElse(request.getRemoteAddr());

        return result;
    }

    protected HttpSession getSession() {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        return request.getSession(false);
    }

    protected Location getLocation() {
        try {
            final String      ip      = this.getRemoteIpAddress();
            final HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest()
                .getSession(true);

            Location location = (Location) session.getAttribute(Constants.SESSION_LOCATION);

            // Refresh location when:
            // (a) not already computed, or
            // (b) the client IP has changed
            //
            // If the location service is not enabled, always create a new
            // location instance using the authenticated account profile
            if (locationService == null && (location == null || location.isEmpty())) {
                location = Location.empty(ip, this.getAccount() == null ? null : this.getAccount().getCountry());
            } else if (locationService != null && (location == null || location.isEmpty() || !location.getIp().equals(ip))) {
                location = locationService.getLocation(ip);
                if (location == null) {
                    location = Location.empty(ip, this.getAccount() == null ? null : this.getAccount().getCountry());
                }
                session.setAttribute(Constants.SESSION_LOCATION, location);
            }

            return location;
        } catch (final Exception ex) {
            logger.error("Failed to compute location", ex.getMessage());
        }

        return null;
    }

    protected RequestContext createContext() {
        return this.createContext(false);
    }

    protected RequestContext createContext(boolean ignoreLogging) {
        final String     ip       = this.getRemoteIpAddress();
        final Location   location = this.getLocation();
        final AccountDto account  = this.authenticationFacade.getCurrentAccount();

        return RequestContext.of(ip, account, location, ignoreLogging);
    }

}
