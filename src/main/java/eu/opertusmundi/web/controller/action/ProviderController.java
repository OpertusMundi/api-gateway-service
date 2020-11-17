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
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = "Provider",
    description = "The asset provider API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface ProviderController {

    /**
     * Save a provider registration request as a draft
     *
     * @param request Updates to apply to the provider profile of the authenticated user
     *
     * @return The updated user profile
     */
    @Operation(
        operationId = "provider-01",
        summary     = "Update provider registration. Roles required: ROLE_USER",
        description =
            "Save a provider registration request as a draft. "
            + "When saving draft data, validation errors are ignored",
        tags        = { "Provider" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/provider/registration/update", consumes = { "application/json" })
    @Secured({ "ROLE_USER" })
    @Validated
    RestResponse<AccountProfileDto> updateRegistration(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Provider registration command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProviderProfessionalCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        ProviderProfessionalCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult);

    /**
     * Submit a provider registration request to the OP platform
     *
     * @param request Updates to apply to the provider profile of the authenticated user
     *
     * @return The updated user profile
     */
    @Operation(
        operationId = "provider-02",
        summary     = "Submit provider registration. Roles required: ROLE_USER",
        description = "Submit a provider registration request to the OP platform",
        tags        = { "Provider" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/provider/registration/submit", consumes = { "application/json" })
    @Secured({ "ROLE_USER" })
    @Validated
    RestResponse<AccountProfileDto> submitRegistration(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Provider registration command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProviderProfessionalCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        ProviderProfessionalCommandDto command,
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
        operationId = "provider-03",
        summary     = "Cancel provider registration. Roles required: ROLE_USER",
        description = "Cancel any pending provider registration request",
        tags        = { "Provider" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @DeleteMapping(value = "/provider/registration/cancel")
    @Secured({ "ROLE_USER" })
    RestResponse<AccountProfileDto> cancelRegistration();

}
