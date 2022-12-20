package eu.opertusmundi.web.controller.api;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.API_Profile,
    description = "The profile API"
)
@SecurityRequirement(name = "jwt")
@RequestMapping(path = "/api", produces = "application/json")
@Secured({"ROLE_API"})
public interface ProfileController {

    /**
     * Get profile data for the authenticated user
     *
     * @return The user profile
     */
    @Operation(
        operationId = "api-profile-01",
        summary     = "Get profile",
        description = "Get user data for the authenticated user."
    )
    @GetMapping(value = "/profile")
    RestResponse<AccountDto> getProfile();

}
