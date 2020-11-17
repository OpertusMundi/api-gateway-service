package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.AccountProfileUpdateCommandDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for user profile operations
 */
@Tag(
    name        = "Profile",
    description = "The profile API"
)
@RequestMapping(path = "/action", produces = "application/json")
@Secured({ "ROLE_USER" })
public interface ProfileController {

    /**
     * Get profile data for the authenticated user
     *
     * @return The user profile
     */
    @Operation(
        operationId = "profile-01",
        summary     = "Get profile. Roles required: ROLE_USER",
        description = "Get profile data for the authenticated user.",
        tags        = { "Profile" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @GetMapping(value = "/profile")
    RestResponse<AccountProfileDto> getProfile();

    /**
     * Update the profile of the authenticated user
     *
     * @param request Updates to apply to the profile of the authenticated user
     *
     * @return The updated user profile
     */
    @Operation(
        operationId = "profile-02",
        summary     = "Update profile. Roles required: ROLE_USER",
        description = "Update the profile of the authenticated user",
        tags        = { "Profile" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/profile", consumes = { "application/json" })
    @Validated
    RestResponse<AccountProfileDto> updateProfile(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Profile update command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountProfileUpdateCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AccountProfileUpdateCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult);

}
