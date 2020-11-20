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
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueAddItemCommandDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientSetStatusCommandDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDraftDetailsDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.web.model.catalogue.client.EnumDraftStatus;
import eu.opertusmundi.web.model.openapi.schema.CatalogueEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
        summary     = "Update registration",
        description =
            "Save a provider registration request as a draft. "
            + "When saving draft data, validation errors are ignored. "
            + "Required roles: ROLE_USER",
        tags        = { "Provider" },
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
        operationId = "provider-02",
        summary     = "Submit registration",
        description = "Submit a provider registration request to the OP platform. "
                      + "Required roles: ROLE_USER",
        tags        = { "Provider" },
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
        operationId = "provider-03",
        summary     = "Cancel registration",
        description = "Cancel any pending provider registration request. "
                      + "Required roles: ROLE_USER",
        tags        = { "Provider" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @DeleteMapping(value = "/provider/registration")
    @Secured({ "ROLE_USER" })
    RestResponse<AccountProfileDto> cancelRegistration();

    /**
     * Search catalogue draft items
     *
     * @param status Item status
     * @param pageIndex Page index
     * @param pageSize Page size
     *
     * @return An instance of {@link CatalogueClientCollectionResponse} class
     */
    @Operation(
        operationId = "provider-04",
        summary     = "Search draft items",
        description = "Search catalogue for provider's draft items based on one or more criteria. Supports data paging and sorting. "
                      + "Required roles: ROLE_PROVIDER",
        tags        = { "Provider" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftCollectionResponse.class)
        )
    )
    @GetMapping(value = "/provider/drafts", consumes = "application/json")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findAllDraft(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Draft status"
        )
        @RequestParam(name = "status", required = true) EnumDraftStatus status,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0") int pageIndex,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "10") int pageSize
    );

    /**
     * Create a new draft item
     *
     * @param command The item to create
     * @return
     */
    @Operation(
        operationId = "provider-05",
        summary     = "Create draft",
        description = "Create draft item. Required roles: ROLE_PROVIDER",
        tags        = { "Provider" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PostMapping(value = "/provider/drafts", consumes = "application/json")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse createDraft(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New draft item.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueAddItemCommandDto.class)),
            required = true
        )
        @RequestBody CatalogueAddItemCommandDto command
    );

    /**
     * Get a single catalogue draft item
     *
     * @param id The item unique id
     * @return A response with a result of type {@link CatalogueItemDto}
     */
    @Operation(
        operationId = "provider-06",
        summary     = "Get draft",
        description = "Get a single catalogue draft item by its unique identifier. "
                      + "Required roles: ROLE_PROVIDER",
        tags        = { "Provider" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class)
        )
    )
    @GetMapping(value = "/provider/drafts/{id}")
    RestResponse<CatalogueItemDraftDetailsDto> findOneDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable UUID id
    );

    /**
     * Update draft item
     *
     * @param id The item unique id
     * @param command The updated item
     * @return
     */
    @Operation(
        operationId = "provider-07",
        summary     = "Update draft",
        description = "Update an existing draft item. Required roles: ROLE_PROVIDER",
        tags        = { "Provider" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/provider/drafts/{id}", consumes = "application/json")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse updateDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable UUID id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated item.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueAddItemCommandDto.class)),
            required = true
        )
        @RequestBody CatalogueAddItemCommandDto command
    );

    /**
     * Set draft item status
     *
     * @param id The item unique id
     * @param command The status update command
     * @return
     */
    @Operation(
        operationId = "provider-08",
        summary     = "Set status",
        description = "Set the status of a draft item. Required roles: ROLE_PROVIDER",
        tags        = { "Provider" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/provider/drafts/{id}/status")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse setDraftStatus(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable UUID id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Update command.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueClientSetStatusCommandDto.class)),
            required = true
        )
        @RequestBody CatalogueClientSetStatusCommandDto command
    );

    /**
     * Delete catalogue draft item
     *
     * @param id The item unique id
     * @return
     */
    @Operation(
        operationId = "provider-09",
        summary     = "Delete draft",
        description = "Delete a draft item. Required roles: ROLE_PROVIDER",
        tags        = { "Provider" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/provider/drafts/{id}")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse deleteDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable UUID id
    );

    /**
     * Delete catalogue item
     *
     * @param id The item unique id
     * @return
     */
    @Operation(
        operationId = "catalogue-11",
        summary     = "Delete asset",
        description = "Delete asset from catalogue. Required roles: ROLE_PROVIDER",
        tags        = { "Catalogue" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/provider/assets/{id}")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse deleteAsset(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable UUID id
    );

}
