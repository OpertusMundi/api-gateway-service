package eu.opertusmundi.web.controller;

import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.MessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.web.security.UserService;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Value("${springdoc.api-docs.server:http://localhost:8080}")
    private String serverUrl;

    @Value("${springdoc.api-docs.path}")
    private String openApiSpec;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService userService;

    /**
     * Default request handler
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Render documentation using the ReDoc {@link https://github.com/Redocly/redoc}
     */
    @GetMapping("/docs")
    public ModelAndView docs() {
        final ModelAndView modelAndView = new ModelAndView("docs");

        modelAndView.getModel().put("endpoint", this.serverUrl + this.openApiSpec);

        return modelAndView;
    }

    /**
     * Request handler for client URLs
     */
    @GetMapping({"/{name:(?!action).*}", "/{name:(?!action).*}/*"})
    public String clientPage() {
        return "index";
    }

    /**
     * Request handler for verifying an email address using a token
     */
    @GetMapping("/token/verify")
    public String verifyToken(@RequestParam UUID token) {
        try {
            final ServiceResponse<Void> response = userService.redeemToken(token);

            if (response.getMessages().isEmpty()) {
                return "redirect:/account/verify-email/success";
            } else {
                logger.warn(String.format("Failed to verify token [token=%s, messages=%s]", token, response.getMessages()));
            }
        } catch (final Exception ex) {
            logger.error("Email address verification has failed [token={}, message={}]", token, ex.getMessage());
        }
        return "redirect:/account/verify-email/failure";
    }

    @GetMapping("/account/verify-email/success")
    public String verifyTokenSuccess() {
        return "index";
    }

    @GetMapping("/account/verify-email/failure")
    public String verifyTokenFailure() {
        return "index";
    }

    /**
     * Handles HTML error pages
     */
    @GetMapping(path = "/error/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String errorHtml(@PathVariable(name = "id", required = true) int id) {
        return "index";
    }

    /**
     * Handles errors for XHR requests
     */
    @GetMapping(path = "/error/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> error(@PathVariable(name = "id", required = true) int id) {
        final HttpStatus  status      = HttpStatus.valueOf(id);
        final MessageCode code        = BasicMessageCode.fromStatusCode(status);
        final String      description = this.messageSource.getMessage(code.key(), null, Locale.getDefault());

        final RestResponse<?> response = RestResponse.error(code, description);

        return new ResponseEntity<>(response, status);
    }

}
