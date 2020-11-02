package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.AccountProfileUpdateCommandDto;
import eu.opertusmundi.common.model.dto.AddressCommandDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

    /**
     * Create new address
     *
     * @param request Address create command
     *
     * @return The updated user profile
     */
    @Operation(
        summary     = "Create address. Roles required: ROLE_USER",
        description = "Add new address to user profile",
        tags        = { "Profile" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/profile/address", consumes = { "application/json" })
    @Validated
    RestResponse<AccountProfileDto> createAddress(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Address create command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AddressCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AddressCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult);

    /**
     * Update address
     *
     * @param request Address update command
     * @param key Address unique key
     *
     * @return The updated user profile
     */
    @Operation(
        summary     = "Update address. Roles required: ROLE_USER",
        description = "Update existing address in user profile",
        tags        = { "Profile" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PutMapping(value = "/profile/address/{key}", consumes = { "application/json" })
    @Validated
    RestResponse<AccountProfileDto> updateAddress(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Address unique key"
        )
        @PathVariable(name = "key", required = true) UUID key,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Address update command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AddressCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AddressCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult);

    /**
     * Delete address
     *
     * @param key Address unique key
     *
     * @return The updated user profile
     */
    @Operation(
        summary     = "Delete address. Roles required: ROLE_USER",
        description = "Delete existing address in user profile",
        tags        = { "Profile" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @DeleteMapping(value = "/profile/address/{key}")
    @Validated
    RestResponse<AccountProfileDto> deleteAddress(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Address unique key"
        )
        @PathVariable(name = "key", required = true) UUID key);

}
