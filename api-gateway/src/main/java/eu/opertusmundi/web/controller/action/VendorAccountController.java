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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumAccountSortField;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
import eu.opertusmundi.common.model.account.VendorAccountCommandDto;
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
 * Endpoint for user authentication actions
 */
@Tag(
    name        = EndpointTags.VendorAccount,
    description = "The vendor account API"
)
@RequestMapping(produces = "application/json")
public interface VendorAccountController {

    /**
     * Browse vendor accounts
     *
     * @param page
     * @param size
     * @param enabled
     * @param email
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "vendor-account-01",
        summary     = "Find",
        description = "Search vendor accounts. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = AccountEndpointTypes.AccountCollectionResponse.class)
        )
    )
    @GetMapping(value = {"/action/vendor-accounts"})
    @Secured({"ROLE_PROVIDER"})
    RestResponse<PageResultDto<AccountDto>> find(
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "25", required = false) @Max(100) @Min(1) Integer size,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "True if only active accounts should be returned"
        )
        @RequestParam(name = "active", required = false) Boolean active,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Optional account email"
        )
        @RequestParam(name = "email", defaultValue = "", required = false) String email,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Order by property"
        )
        @RequestParam(name = "orderBy", defaultValue = "EMAIL", required = false) EnumAccountSortField orderBy,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Sorting order"
        )
        @RequestParam(name = "order",   defaultValue = "ASC", required = false)   EnumSortingOrder order
    );

    /**
     * Creates a new vendor account
     *
     * @param command
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "vendor-account-02",
        summary     = "Create",
        description = "Creates a new vendor account. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountEndpointTypes.AccountResponse.class))
    )
    @PostMapping(value = "/action/vendor-accounts")
    @Validated
    @Secured({"ROLE_PROVIDER"})
    BaseResponse create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Account create command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VendorAccountCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        VendorAccountCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Updates an existing vendor account
     *
     * @param key
     * @param command
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "vendor-account-03",
        summary     = "Update",
        description = "Updates an existing vendor account. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountEndpointTypes.AccountResponse.class))
    )
    @PutMapping(value = "/action/vendor-accounts/{key}")
    @Validated
    @Secured({"ROLE_PROVIDER"})
    BaseResponse update(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Account unique key"
        )
        @PathVariable UUID key,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Account update command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VendorAccountCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        VendorAccountCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Delete an existing vendor account
     *
     * Only accounts with activation status
     * {@link EnumActivationStatus#UNDEFINED} can be deleted
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "vendor-account-04",
        summary     = "Delete",
        description = "Deletes an existing vendor account. Only accounts with activation status `UNDEFINED` can be deleted. "
                    + "Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/action/vendor-accounts/{key}")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse delete(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Account unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Invite vendor account to join provider organization
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "vendor-account-05",
        summary     = "Invite",
        description = "Invites a user to activate her vendor account. "
                    + "An email is sent with a new activation token. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountEndpointTypes.AccountResponse.class))
    )
    @PostMapping(value = "/action/vendor-accounts/{key}/invites")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse invite(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Account unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Join organization
     *
     * @param token
     * @return
     */
    @Operation(
        operationId = "vendor-account-06",
        summary     = "Join",
        description = "Accepts vendor invitation to join an organization"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json")
    )
    @PostMapping(value = "/action/vendor-accounts/invites/{token}")
    @Validated
    BaseResponse acceptInvite(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Activation token"
        )
        @PathVariable UUID token
    );

    /**
     * Enable account
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "vendor-account-07",
        summary     = "Enable",
        description = "Enable vendor account. A vendor can enable only accounts with activation status `COMPLETED`. "
                    + "If the account activation status is `UNDEFINED` an error is returned. If the status is either "
                    + "`PENDING` or `PROCESSING`, the action is ignored. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountEndpointTypes.AccountResponse.class))
    )
    @PutMapping(value = "/action/vendor-accounts/{key}/status")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse enable(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Account unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Disable account
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "vendor-account-08",
        summary     = "Disable",
        description = "Disable vendor account. If the account activation status is either `PENDING` or `PROCESSING`, "
                    + "the action is ignored. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description  = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountEndpointTypes.AccountResponse.class))
    )
    @DeleteMapping(value = "/action/vendor-accounts/{key}/status")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse disable(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Account unique key"
        )
        @PathVariable UUID key
    );

}
