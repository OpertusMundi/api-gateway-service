package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountProfileDto;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.account.ProviderProfileCommandDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.ProviderRegistration,
    description = "The provider registration API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface ProviderRegistrationController {

    /**
     * Save a provider registration request as a draft
     *
     * @param request Updates to apply to the provider profile of the authenticated user
     *
     * @return The updated user profile
     */
    @Operation(
        operationId = "provider-registration-01",
        summary     = "Update registration",
        description =
            "Save a provider registration request as a draft. "
            + "When saving draft data, validation errors are ignored. "
            + "Required role: `ROLE_USER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/provider/registration", consumes = { "application/json" })
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
        operationId = "provider-registration-02",
        summary     = "Submit registration",
        description = "Submit a provider registration request to the OP platform. "
                      + "Required role: `ROLE_USER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PutMapping(value = "/provider/registration", consumes = { "application/json" })
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
        operationId = "provider-registration-03",
        summary     = "Cancel registration",
        description = "Cancel any pending provider registration request. "
                      + "Required role: `ROLE_USER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @DeleteMapping(value = "/provider/registration")
    @Secured({ "ROLE_USER" })
    RestResponse<AccountProfileDto> cancelRegistration();

    /**
     * Update provider profile
     *
     * @param request Updates to provider's profile
     *
     * @return The updated user profile
     */
    @Operation(
        operationId = "provider-registration-04",
        summary     = "Update profile",
        description = "Update optional properties of a provider's profile. Required role: `ROLE_PROVIDER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/provider/profile", consumes = { "application/json" })
    @Secured({ "ROLE_PROVIDER" })
    @Validated
    RestResponse<AccountProfileDto> updateProfile(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Update profile command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProviderProfileCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        ProviderProfileCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult);
}
