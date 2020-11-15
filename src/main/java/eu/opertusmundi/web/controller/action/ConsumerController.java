package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.ConsumerIndividualCommandDto;
import eu.opertusmundi.common.model.dto.ConsumerProfessionalCommandDto;
import eu.opertusmundi.common.model.dto.CustomerCommandDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = "Consumer",
    description = "The consumer API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface ConsumerController {

    /**
     * Create a new or update an existing draft for consumer data in the profile
     * of the authenticated user
     *
     * @param request Updates to be applied to the profile of the authenticated user
     *
     * @return The updated user profile
     */
    @Operation(
        summary     = "Update consumer registration. Roles required: ROLE_USER",
        description =
            "Create or update consumer draft data in the profile of the authenticated user. "
            + "When saving draft data, validation errors are ignored",
        tags        = { "Consumer" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/consumer/registration/update", consumes = { "application/json" })
    @Secured({ "ROLE_USER" })
    @Validated
    RestResponse<AccountProfileDto> updateRegistration(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Consumer registration command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(oneOf = {ConsumerIndividualCommandDto.class, ConsumerProfessionalCommandDto.class})
            ),
            required = true
        )
        @Valid
        @RequestBody
        CustomerCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult);

    /**
     * Update consumer data in the profile of the authenticated user
     *
     * @param request Updates to be applied to the profile of the authenticated user
     *
     * @return The updated user profile
     */
    @Operation(
        summary     = "Submit consumer registration. Roles required: ROLE_USER",
        description = "Update consumer data in the profile of the authenticated user.",
        tags        = { "Consumer" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/consumer/registration/submit", consumes = { "application/json" })
    @Secured({ "ROLE_USER" })
    @Validated
    RestResponse<AccountProfileDto> submitRegistration(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Consumer registration command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(oneOf = {ConsumerIndividualCommandDto.class, ConsumerProfessionalCommandDto.class})
            ),
            required = true
        )
        @Valid
        @RequestBody
        CustomerCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult);


    /**
     * Cancel any pending registration request
     *
     * @return The updated user profile
     */
    @Operation(
        summary     = "Cancel consumer registration. Roles required: ROLE_USER",
        description = "Cancel any pending consumer registration request",
        tags        = { "Consumer" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @DeleteMapping(value = "/consumer/registration/cancel")
    @Secured({ "ROLE_USER" })
    RestResponse<AccountProfileDto> cancelRegistration();

}
