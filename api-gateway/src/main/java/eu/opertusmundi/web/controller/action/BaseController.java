package eu.opertusmundi.web.controller.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.RequestContext;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.location.Location;
import eu.opertusmundi.common.service.LocationService;
import eu.opertusmundi.web.model.Constants;
import eu.opertusmundi.web.security.AuthenticationFacade;

public abstract class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

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

    protected Integer currentUserParentId() {
        return this.authenticationFacade.getCurrentUserParentId();
    }

    protected UUID currentUserKey() {
        return this.authenticationFacade.getCurrentUserKey();
    }

    protected UUID currentUserParentKey() {
        return this.authenticationFacade.getCurrentUserParentKey();
    }

    protected String currentUserEmail() {
        return this.authenticationFacade.getCurrentUserEmail();
    }

    protected boolean hasRole(EnumRole role) {
        return this.authenticationFacade.hasRole(role);
    }

    protected boolean hasAnyRole(EnumRole... roles) {
        return this.authenticationFacade.hasAnyRole(roles);
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

    protected ResponseEntity<StreamingResponseBody> createDownloadResponsePdf(
            HttpServletResponse response, File file, String downloadFilename
    ) {
        return this.createDownloadResponse(response, file, downloadFilename, MediaType.APPLICATION_PDF);
    }

    protected ResponseEntity<StreamingResponseBody> createDownloadResponse(
        HttpServletResponse response, File file, String downloadFilename, MediaType mediaType
    ) {
        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", downloadFilename));
        response.setHeader("Content-Type", mediaType.toString());
        if (file.length() < 1024 * 1024) {
            response.setHeader("Content-Length", Long.toString(file.length()));
        }

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }

}
