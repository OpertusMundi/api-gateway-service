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
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftSortField;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.asset.UserFileResourceCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemMetadataCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftFromAssetCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.openapi.schema.CatalogueEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.Draft,
    description = "The asset publication API"
)
@RequestMapping(path = "/action", produces = "application/json")
@Secured({"ROLE_PROVIDER", "ROLE_VENDOR_PROVIDER"})
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
        operationId = "draft-asset-01",
        summary     = "Search drafts",
        description = "Search catalogue for provider's draft items based on one or more criteria. Supports data paging and sorting. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftCollectionResponse.class)
        )
    )
    @GetMapping(value = "/drafts")
    RestResponse<?> findAllDraft(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Draft status"
        )
        @RequestParam(name = "status", required = true) Set<EnumProviderAssetDraftStatus> status,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Draft asset type"
        )
        @RequestParam(name = "type", required = false) Set<EnumAssetType> type,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Draft service type. Applicable only for asset type `SERVICE`"
        )
        @RequestParam(name = "serviceType", required = false) Set<EnumSpatialDataServiceType> serviceType,
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
     * @param command
     * @param lock
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "draft-asset-02a",
        summary     = "Create draft",
        description = "Create draft item. By default the new record is locked. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/drafts", consumes = "application/json")
    @Validated
    RestResponse<AssetDraftDto> createDraft(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "`true` if the new record must be also locked"
        )
        @RequestParam(name = "lock", required = false, defaultValue = "true")
        boolean lock,
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
     * Create a new draft item from an existing asset
     *
     * @param command
     * @param lock
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "draft-asset-02b",
        summary     = "Create draft from asset",
        description = "Create a new draft item from an existing asset. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/drafts/asset", consumes = "application/json")
    @Validated
    RestResponse<AssetDraftDto> createDraftFromAsset(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "`true` if the new record must also be locked"
        )
        @RequestParam(name = "lock", required = false, defaultValue = "true") boolean lock,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Draft create command",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DraftFromAssetCommandDto.class)),
            required = true
        )
        @Valid
        @RequestBody
        DraftFromAssetCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Get a single catalogue draft item
     *
     * @param draftKey The item unique key
     * @param lock
     * @return A response with a result of type {@link CatalogueItemDto}
     */
    @Operation(
        operationId = "draft-asset-03",
        summary     = "Get draft",
        description = "Get a single catalogue draft item by its unique identifier. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class)
        )
    )
    @GetMapping(value = "/drafts/{draftKey}")
    RestResponse<AssetDraftDto> findOneDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "`true` if the selected record must be also locked. If a lock already exists "
                        + "and belongs to another user, an error is returned. If no lock is requested, but "
                        + "an existing one is found, it is <b>not</b> released."
        )
        @RequestParam(name = "lock", required = false, defaultValue = "false") boolean lock
    );

    /**
     * Update draft item
     *
     * @param draftKey The item unique key
     * @param command The updated item
     * @param lock
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "draft-asset-04",
        summary     = "Update draft",
        description = "Update an existing draft item. Resources that are not included in the request, are automatically "
                    + "deleted. If `lock` parameter is `true`, the record remains locked. Otherwise, the lock is released "
                    + "once the record is successfully saved. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PutMapping(value = "/drafts/{draftKey}", consumes = "application/json")
    @Validated
    RestResponse<AssetDraftDto> updateDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "`true` if the record must remain locked after a successful save operation. "
                        + "If a lock already exists and belongs to another user, an error is returned."
        )
        @RequestParam(name = "lock", required = false, defaultValue = "false") boolean lock,
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
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "draft-asset-05",
        summary     = "Submit existing draft",
        description = "Update draft and submit for review and publication. The lock on the record is automatically released. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/drafts/{draftKey}/submit")
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
        operationId = "draft-asset-06",
        summary     = "Review draft",
        description = "Accept or reject draft by a provider. If the draft is locked by another user,"
                    + "the operation will fail. The draft status must be `PENDING_PROVIDER_REVIEW`. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/drafts/{draftKey}/review")
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
        operationId = "draft-asset-07",
        summary     = "Delete draft",
        description = "Delete a draft item. If the record is locked by another user, the operation will fail. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/drafts/{draftKey}")
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
        operationId = "draft-asset-08a",
        summary     = "Upload resource",
        description = "Uploads a resource file and links it to selected draft instance. On success, an updated draft is returned "
                    + "with the new resource registration. If the record is locked by another user, the operation will fail. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/drafts/{draftKey}/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
        @Valid @RequestPart(name = "data", required = true) FileResourceCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Add a resource file from the user's file system
     *
     * @param draftKey Draft unique key
     * @param command The selected file to add as a resource
     * @param validationResult
     */
    @Operation(
        operationId = "draft-asset-08b",
        summary     = "Add resource",
        description = "Uploads a file and links it to selected draft instance or adds a resource from an "
                    + "existing file in the user's file system. On success, an updated draft is returned "
                    + "with the new resource registration. If the record is locked by another user, the operation "
                    + "will fail. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/drafts/{draftKey}/resources", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Validated
    RestResponse<?> addResourceFromFileSystem(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
        @Valid @RequestBody UserFileResourceCommandDto command,
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
        operationId = "draft-asset-09",
        summary     = "Upload additional resource",
        description = "Uploads an additional resource file and links it to selected draft instance. On success, an updated draft is returned "
                    + "with the new resource registration. If the record is locked by another user, the operation will fail. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`",
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
    @PostMapping(value = "/drafts/{draftKey}/additional-resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
        operationId = "draft-asset-10",
        summary     = "Download additional resource",
        description = "Downloads an additional resource file. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/drafts/{draftKey}/additional-resources/{resourceKey}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
        @PathVariable String resourceKey,
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
        operationId = "draft-asset-11",
        summary     = "Get metadata property",
        description = "Gets metadata property value for the specified resource file. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`",
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
        value = "/drafts/{draftKey}/resources/{resourceKey}/metadata/{propertyName}",
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
        @PathVariable String resourceKey,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Property name"
        )
        @PathVariable String propertyName,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

    /**
     * Create an API draft from an existing published asset or a file in the
     * user's file system
     *
     * @param command
     * @param lock
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "assets-12",
        summary     = "Create API draft",
        description = "Create a new API draft from an existing published asset or a file in user's file system. "
                    + "By default, a lock is acquired for the new record. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/drafts/api")
    @Validated
    BaseResponse createApiDraft(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "`true` if the new record must also be locked"
        )
        @RequestParam(name = "lock", required = false, defaultValue = "true")
        boolean lock,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Draft creation command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DraftApiCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        DraftApiCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Update draft metadata property visibility
     *
     * @param draftKey The item unique key
     * @param command The update command
     * @return
     */
    @Operation(
        operationId = "draft-asset-13",
        summary     = "Update metadata",
        description = "Update the metadata properties of an existing draft item that "
                    + "has been accepted by the Helpdesk application. The draft status "
                    + "must be `PENDING_PROVIDER_REVIEW`. If the record is locked by "
                    + "another user, the operation will fail. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.DraftItemResponse.class))
    )
    @PostMapping(value = "/drafts/{draftKey}/metadata", consumes = "application/json")
    @Validated
    RestResponse<AssetDraftDto> updateDraftMetadata(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated item.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CatalogueItemMetadataCommandDto.class
            )),
            required = true
        )
        @Valid
        @RequestBody
        CatalogueItemMetadataCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Release lock
     *
     * @param draftKey The draft unique key
     * @return
     */
    @Operation(
        operationId = "draft-asset-15",
        summary     = "Release lock",
        description = "Release the record lock if the authenticated user already owns it. "
                    + "If the record is not locked, the request is ignored. If a lock exists "
                    + "and belongs to another user, an error is returned. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json"
        )
    )
    @DeleteMapping(value = "/drafts/{draftKey}/lock")
    BaseResponse releaseLock(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey
    );

}
