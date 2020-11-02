package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountProfileConsumerCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
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
     * Update consumer data in the profile of the authenticated user
     *
     * @param request Updates to apply to the profile of the authenticated user
     *
     * @return The updated user profile
     */
    @Operation(
        summary     = "Update consumer. Roles required: ROLE_CONSUMER",
        description = "Update consumer data in the profile of the authenticated user",
        tags        = { "Consumer" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/consumer", consumes = { "application/json" })
    @Secured({ "ROLE_USER", "ROLE_CONSUMER" })
    @Validated
    RestResponse<AccountProfileDto> update(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Profile update command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountProfileConsumerCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AccountProfileConsumerCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult);

}
