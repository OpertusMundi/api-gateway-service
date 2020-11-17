package eu.opertusmundi.web.controller;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.DebugRestResponse;
import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.MessageCode;
import eu.opertusmundi.common.model.RestResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@ControllerAdvice(annotations = { RestController.class })
public class RestControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(RestControllerAdvice.class);

    private static final String DEVELOPMENT_PROFILE = "development";

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Autowired
    private MessageSource messageSource;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ApiResponse(
        responseCode = "400",
        description = "Bad Request",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DebugRestResponse.class))
    )
    public @ResponseBody BaseResponse handleException(HttpMessageNotReadableException ex) {

        logger.error("Cannot parse input:" + ex.getMessage(), ex);

        final MessageCode code        = BasicMessageCode.BadRequest;
        final String      description = this.messageSource.getMessage(code.key(), null, Locale.getDefault());

        final Message error = new Message(BasicMessageCode.BadRequest, description, Message.EnumLevel.ERROR);

        if (this.isDevelopmentProfileActive()) {
            return new DebugRestResponse(error, ex.getMessage(), ex);
        }

        return RestResponse.error(code, description);
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
