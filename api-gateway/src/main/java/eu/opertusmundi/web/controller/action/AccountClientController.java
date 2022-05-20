package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountClientCommandDto;
import eu.opertusmundi.common.model.account.AccountClientDto;
import eu.opertusmundi.web.model.openapi.schema.AccountEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for account clients
 */
@Tag(
    name        = EndpointTags.AccountClient,
    description = "The account client API"
)
@RequestMapping(produces = "application/json")
public interface AccountClientController {

    /**
     * Find all clients
     *
     * @param page
     * @param size
     * @return
     */
    @Operation(
        operationId = "account-client-01",
        summary     = "Find",
        description = "Get all account clients. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = AccountEndpointTypes.AccountClientCollectionResponse.class)
        )
    )
    @GetMapping(value = {"/action/account/clients"})
    @Secured({"ROLE_CONSUMER"})
    RestResponse<PageResultDto<AccountClientDto>> find(
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "25", required = false) @Max(100) @Min(1) Integer size
    );

    /**
     * Creates a client
     *
     * @param command
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "account-client-02",
        summary     = "Create",
        description = "Creates a new account client. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountEndpointTypes.AccountClientResponse.class))
    )
    @PostMapping(value = "/action/account/clients")
    @Validated
    @Secured({"ROLE_CONSUMER"})
    BaseResponse create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Client create command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountClientCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AccountClientCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Revoke an existing client
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "account-client-03",
        summary     = "Revoke",
        description = "Revokes an existing account client. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/action/account/clients/{key}")
    @Secured({"ROLE_CONSUMER"})
    BaseResponse revoke(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Client unique key"
        )
        @PathVariable UUID key
    );

}
