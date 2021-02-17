package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetResourceCommandDto;
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
        @RequestParam(name = "orderBy", defaultValue = "CREATED_ON") EnumProviderAssetDraftSortField orderBy,
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
        description = "Update an existing draft item. Resources that are not included in the request, are automatically "
                    + "deleted. Required roles: <b>ROLE_PROVIDER</b>"
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
     * Uploads a resource file
     *
     * @param draftKey Draft unique key
     * @param file An instance of {@link MultipartFile} with the uploaded file
     * @param command Metadata for the uploaded file
     * @param validationResult
     */
    @Operation(
        operationId = "provider-draft-asset-09",
        summary     = "Upload resource",
        description = "Uploads a resource file and links it to selected draft instance. On success, an updated draft is returned "
                    + "with the new resource registration."
                    + "Roles required: <b>ROLE_PROVIDER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/provider/drafts/{draftKey}/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Validated
    RestResponse<?> uploadResource(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(schema = @Schema(
            name = "file", type = "string", format = "binary", description = "Uploaded file"
        ))
        @NotNull @RequestPart(name = "file", required = true) MultipartFile file,
        @Valid @RequestPart(name = "data", required = true) AssetResourceCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
   
    /**
     * Uploads an additional resource file
     * 
     * @param draftKey Draft unique key
     * @param file An instance of {@link MultipartFile} with the uploaded file
     * @param command Metadata for the uploaded file
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "provider-draft-asset-10",
        summary     = "Upload additional resource",
        description = "Uploads an additional resource file and links it to selected draft instance. On success, an updated draft is returned "
                    + "with the new resource registration."
                    + "Roles required: <b>ROLE_PROVIDER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class)
        )
    )
    @PostMapping(value = "/provider/drafts/{draftKey}/additional-resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Validated
    RestResponse<?> uploadAdditionalResource(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(schema = @Schema(
            name = "file", type = "string", format = "binary", description = "Uploaded file"
        ))
        @NotNull @RequestPart(name = "file", required = true) MultipartFile file,
        @Valid @RequestPart(name = "data", required = true) AssetFileAdditionalResourceCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Download an additional resource file
     *
     * @param draftKey Draft unique key
     * @param resourceKey Resource unique key
     * 
     * @return The requested file
     */
    @Operation(
        operationId = "provider-draft-asset-11",
        summary     = "Download additional resource",
        description = "Downloads an additional resource file. Roles required: <b>ROLE_PROVIDER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/provider/drafts/{draftKey}/additional-resources/{resourceKey}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Resource unique key"
        )
        @PathVariable UUID resourceKey,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

    /**
     * Get metadata property value
     *
     * @param draftKey Draft unique key
     * @param resourceKey Resource unique key
     * @param propertyName The property name
     * 
     * @return The requested property value
     */
    @Operation(
        operationId = "provider-draft-asset-11",
        summary     = "Get metadata property",
        description = "Gets metadata property value for the specified resource file. Roles required: <b>ROLE_PROVIDER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested value"))
    )
    @GetMapping(
        value = "/provider/drafts/{draftKey}/resources/{resourceKey}/metadata/{propertyName}", 
        produces = {MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    ResponseEntity<StreamingResponseBody> getMetadataProperty(
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Resource unique key"
        )
        @PathVariable UUID resourceKey,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Property name"
        )
        @PathVariable String propertyName,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;
    
}
