package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftSortField;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.dto.EnumSortingOrder;
import eu.opertusmundi.common.model.openapi.schema.CatalogueEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = "Provider Drafts",
    description = "The provider asset publication API"
)
@RequestMapping(path = "/action", produces = "application/json")
@Secured({"ROLE_PROVIDER"})
public interface ProviderDraftAssetController {

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
        operationId = "provider-draft-asset-01",
        summary     = "Search draft items",
        description = "Search catalogue for provider's draft items based on one or more criteria. Supports data paging and sorting. "
                      + "Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftCollectionResponse.class)
        )
    )
    @GetMapping(value = "/provider/drafts", consumes = "application/json")
    RestResponse<?> findAllDraft(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Draft status"
        )
        @RequestParam(name = "status", required = true) Set<EnumProviderAssetDraftStatus> status,
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
        @RequestParam(name = "size", defaultValue = "10") int pageSize,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order by property"
        )
        @RequestParam(name = "orderBy", defaultValue = "name") EnumProviderAssetDraftSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Create a new draft item
     *
     * @param command The item to create
     * @return
     */
    @Operation(
        operationId = "provider-draft-asset-02",
        summary     = "Create draft",
        description = "Create draft item. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/provider/drafts", consumes = "application/json")
    @Validated
    RestResponse<AssetDraftDto> createDraft(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New draft item.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueItemCommandDto.class)),
            required = true
        )
        @Valid
        @RequestBody
        CatalogueItemCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Get a single catalogue draft item
     *
     * @param draftKey The item unique key
     * @return A response with a result of type {@link CatalogueItemDto}
     */
    @Operation(
        operationId = "provider-draft-asset-03",
        summary     = "Get draft",
        description = "Get a single catalogue draft item by its unique identifier. "
                      + "Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class)
        )
    )
    @GetMapping(value = "/provider/drafts/{draftKey}")
    RestResponse<AssetDraftDto> findOneDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey
    );

    /**
     * Update draft item
     *
     * @param draftKey The item unique key
     * @param command The updated item
     * @return
     */
    @Operation(
        operationId = "provider-draft-asset-04",
        summary     = "Update draft",
        description = "Update an existing draft item. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PutMapping(value = "/provider/drafts/{draftKey}", consumes = "application/json")
    @Validated
    RestResponse<AssetDraftDto> updateDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated item.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueItemCommandDto.class)),
            required = true
        )
        @Valid
        @RequestBody
        CatalogueItemCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Update existing draft and submit for review
     *
     * @param draftKey The item unique key
     * @param command The status update command
     * @return
     */
    @Operation(
        operationId = "provider-draft-asset-05",
        summary     = "Submit existing draft",
        description = "Update draft and submit for review and publication. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/provider/drafts/{draftKey}/submit")
    @Validated
    BaseResponse submitDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Update command.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueItemCommandDto.class)),
            required = true
        )
        @Valid
        @RequestBody
        CatalogueItemCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Create new draft and submit for review
     *
     * @param command The status update command
     * @return
     */
    @Operation(
        operationId = "provider-draft-asset-06",
        summary     = "Submit new draft",
        description = "Create new draft and submit for review and publication. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/provider/drafts")
    @Validated
    BaseResponse saveAndSubmitDraft(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Update command.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueItemCommandDto.class)),
            required = true
        )
        @Valid
        @RequestBody
        CatalogueItemCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Review draft
     *
     * @param draftKey The item unique key
     * @param command The status update command
     * @return
     */
    @Operation(
        operationId = "provider-draft-asset-07",
        summary     = "Review draft",
        description = "Accept or reject draft by a provider. "
                      + "The draft status must be <b>PENDING_PROVIDER_REVIEW</b>. "
                      + "Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/provider/drafts/{draftKey}/review")
    BaseResponse reviewDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Review command.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetDraftReviewCommandDto.class)),
            required = true
        )
        @RequestBody AssetDraftReviewCommandDto command
    );

    /**
     * Delete catalogue draft item
     *
     * @param draftKey The item unique key
     * @return
     */
    @Operation(
        operationId = "provider-draft-asset-08",
        summary     = "Delete draft",
        description = "Delete a draft item. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/provider/drafts/{draftKey}")
    BaseResponse deleteDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey
    );

    /**
     * Uploads a file
     *
     * @param files An array of {@link MultipartFile} with the uploaded files.
     * @return the updated draft
     */
    @Operation(
        operationId = "provider-draft-asset-09",
        summary     = "Upload files",
        description = "Uploads one or more files and links them to selected draft instance. Roles required: <b>ROLE_PROVIDER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/provider/drafts/{draftKey}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    RestResponse<AssetDraftDto> uploadFiles(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(
            description = "Array of uploaded files",
            array = @ArraySchema(
                arraySchema = @Schema(name = "file", type = "string", format = "binary", description = "Uploaded file"),
                minItems = 1
            )
        )
        @RequestPart(name = "files", required = true) MultipartFile[] files
    ) throws AccessDeniedException;

    /**
     * Delete a file
     *
     * @param fileName Name of the file to delete
     * @return the updated draft
     */
    @Operation(
        operationId = "provider-draft-asset-10",
        summary     = "Delete file",
        description = "Deletes a file. Roles required: <b>ROLE_PROVIDER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @DeleteMapping(value = "/provider/drafts/{draftKey}/files", params = {"path"})
    public RestResponse<?> deleteFile(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = true,
            description = "Path of the file to delete. The file must exist in the draft files collection"
        )
        @RequestParam(name = "path", required = true) String path
    ) throws AccessDeniedException;

}
