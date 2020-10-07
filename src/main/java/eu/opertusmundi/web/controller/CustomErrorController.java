package eu.opertusmundi.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.DebugRestResponse;
import eu.opertusmundi.common.model.Message.EnumLevel;
import eu.opertusmundi.common.model.MessageCode;
import eu.opertusmundi.common.model.RestResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Custom error controller that overrides default error handling behavior for
 * browser and API requests.
 *
 * For browser requests, the default "whitelabel" error view is replaced. The
 * HTTP status code is always set to OK (200). For 404 errors, the default index
 * view is returned and navigation is deferred to the client-side router. Any
 * other error is redirected to client-side /error/500 route.
 *
 * For API requests, a response structured like an instance {@link RestResponse}
 * is returned to the client. The response contains a single error object. When
 * development profile is active, an additional property is returned containing
 * any exception data available.
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomErrorController extends BasicErrorController {

    private static final String DEVELOPMENT_PROFILE = "development";

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    private MessageSource messageSource;

    public CustomErrorController(
        ErrorAttributes errorAttributes,
        ServerProperties serverProperties,
        ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider
    ) {
        super(errorAttributes, serverProperties.getError(), errorViewResolversProvider.getIfAvailable());
    }

    @Override
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        final HttpStatus status = this.getStatus(request);

        // Always set status code to OK (200)
        response.setStatus(HttpStatus.OK.value());

        // For 404 error return the default page
        if (status == HttpStatus.NOT_FOUND) {
            return new ModelAndView("index");
        }

        // For all other errors redirect to 500 error page
        return new ModelAndView("redirect:/error/500");
    }

    @Override
    @RequestMapping
    @ApiResponse(
        responseCode = "200",
        description = "Failed operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DebugRestResponse.class))
    )
    @ApiResponse(
        responseCode = "204",
        description = "No Content",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DebugRestResponse.class))
    )
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        final HttpStatus status = this.getStatus(request);

        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }

        // Construct a RestResponse like object
        final MessageCode code = BasicMessageCode.fromStatusCode(status);
        final Map<String, Object> message = new HashMap<String, Object>();

        message.put("code", code.key());
        message.put("level", EnumLevel.ERROR);
        message.put("description", this.messageSource.getMessage(code.key(), null, Locale.getDefault()));

        // For development profile, additional debug properties are returned
        if (this.isDevelopmentProfileActive()) {
            final ErrorAttributeOptions options = ErrorAttributeOptions.of(
                Include.MESSAGE,
                Include.EXCEPTION,
                Include.STACK_TRACE,
                Include.BINDING_ERRORS
            );

            final Map<String, Object> debug = this.getErrorAttributes(request, options);
            message.put("debug", debug);
        }

        final ArrayList<Object> messages = new ArrayList<Object>();
        messages.add(message);

        final Map<String, Object> response = new HashMap<String, Object>();

        response.put("success", false);
        response.put("result", null);
        response.put("messages", messages);

        // Always set status code to OK (200)
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isDevelopmentProfileActive() {
        for (final String profileName : this.activeProfile.split(",")) {
            if (profileName.equalsIgnoreCase(DEVELOPMENT_PROFILE)) {
                return true;
            }
        }
        return false;
    }

}