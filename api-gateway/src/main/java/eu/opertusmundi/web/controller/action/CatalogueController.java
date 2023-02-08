package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueJoinableItemDto;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.openapi.schema.CatalogueEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for accessing catalogue data
 */
@Tag(
    name        = EndpointTags.Catalogue,
    description = "The catalogue API"
)
@RequestMapping(path = "/action", produces = MediaType.APPLICATION_JSON_VALUE)
public interface CatalogueController {

    /**
     * Search catalogue published items
     *
     * @param request The search criteria
     * @return An instance of {@link CatalogueClientCollectionResponse} class
     */
    @Operation(
        operationId = "catalogue-01",
        summary     = "Find",
        description = "Search catalogue published items based on one or more criteria using Elasticsearch."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemCollectionResponse.class)
        )
    )
    @GetMapping(value = "/catalogue")
    RestResponse<?> findAll(
        @ParameterObject()
        ElasticAssetQuery query
    );

    /**
     * Search catalogue published items
     *
     * @param request The search criteria
     * @return An instance of {@link CatalogueClientCollectionResponse} class
     */
    @Operation(
        operationId = "catalogue-02",
        summary     = "Find Autocomplete",
        description = "Search catalogue published items based on one or more criteria using Elasticsearch. "
                    + "Search keywords are not added to the user's recent search history"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemCollectionResponse.class)
        )
    )
    @GetMapping(value = "/catalogue/autocomplete")
    RestResponse<?> findAllAutocomplete(
        @ParameterObject()
        ElasticAssetQuery query
    );

    /**
     * Get a single catalogue item
     *
     * @param id The item unique id
     * @return A response with a result of type {@link CatalogueItemDto}
     */
    @Operation(
        operationId = "catalogue-03a",
        summary     = "Get asset by id",
        description = "Get a single catalogue item by its unique identifier."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemResponse.class)
        )
    )
    @GetMapping(value = "/catalogue/items/{id}")
    RestResponse<CatalogueItemDetailsDto> findOne(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable String id
    );

    /**
     * Get a single catalogue item
     *
     * @param id The item unique id
     * @param version The item version
     * @return A response with a result of type {@link CatalogueItemDto}
     */
    @Operation(
        operationId = "catalogue-03b",
        summary     = "Get asset by id and version",
        description = "Get a single catalogue item by its unique identifier and version."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemResponse.class)
        )
    )
    @GetMapping(value = "/catalogue/history/items/{id}")
    RestResponse<CatalogueItemDetailsDto> findOne(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable String id,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Item version"
        )
        @RequestParam(name = "version", required = true) String version
    );

    /**
     * Get a single joinable catalogue item
     *
     * @param id The item unique id
     * @return A response with a result of type {@link CatalogueJoinableItemDto}
     */
    @Operation(
        operationId = "catalogue-03c",
        summary     = "Get joinable asset",
        description = "Get a single joinable catalogue item by its unique identifier."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.JoinableItemResponse.class)
        )
    )
    @GetMapping(value = "/catalogue/joinable-items/{id}")
    RestResponse<CatalogueJoinableItemDto> findOneJoinable(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable String id
    );

    /**
     * Harvest catalogue
     *
     * @param command Harvest operation settings
     * @return
     */
    @Operation(
        operationId = "catalogue-04",
        summary     = "Harvest catalogue",
        description = "Harvest catalogue for metadata. The user can optionally select the type of the "
                    + "catalogue. If type is not set, the default `EnumCatalogueType.CSW` value is used. "
                    + "Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class)
        )
    )
    @PostMapping(value = "/catalogue/harvest", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_PROVIDER"})
    @Validated
    RestResponse<Void> harvestCatalogue(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Harvest operation settings",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema    = @Schema(implementation = CatalogueHarvestCommandDto.class)
            ),
            required = true
        )
        @Valid @RequestBody CatalogueHarvestCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Search catalogue harvested items
     *
     * @param url Catalogue URL
     * @param pageIndex Page index
     * @param pageSize Page size
     *
     * @return An instance of {@link CatalogueClientCollectionResponse} class
     */
    @Operation(
        operationId = "catalogue-05",
        summary     = "Search harvested assets",
        description = "Search external catalogue harvested items. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemCollectionResponse.class)
        )
    )
    @GetMapping(value = "/catalogue/harvest")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findAllHarvested(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Catalogue URL"
        )
        @RequestParam(name = "url", required = true) String url,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Search query"
        )
        @RequestParam(name = "query") String query,
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
     * Create drafts for items harvested from an external catalogue
     *
     * @param command Import operation settings
     * @return
     */
    @Operation(
        operationId = "catalogue-06",
        summary     = "Import drafts",
        description = "Create drafts from items harvested from an external catalogue. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.HarvestImportResponse.class)
        )
    )
    @PostMapping(value = "/catalogue/harvest/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_PROVIDER"})
    @Validated
    RestResponse<?> importFromCatalogue(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Import operation settings",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema    = @Schema(implementation = CatalogueHarvestImportCommandDto.class)
            ),
            required = true
        )
        @Valid @RequestBody CatalogueHarvestImportCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Find related assets
     *
     * @param id The item unique id
     * @return An instance of {@link CatalogueEndpointTypes.ItemCollectionResponse} class
     */
    @Operation(
        operationId = "catalogue-07",
        summary     = "Find related assets",
        description = "Find related assets given an asset identifier."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemCollectionResponse.class)
        )
    )
    @GetMapping(value = "/catalogue/{id}/related")
    RestResponse<?> findAllRelatedAssets(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable String id
    );

    /**
     * Find bundles that contain an asset
     *
     * @param id The asset unique id
     * @return An instance of {@link CatalogueEndpointTypes.ItemCollectionResponse} class
     */
    @Operation(
        operationId = "catalogue-07",
        summary     = "Find related bundles",
        description = "Find related collection of assets (bundles) that contain the specified asset identifier."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemCollectionResponse.class)
        )
    )
    @GetMapping(value = "/catalogue/{id}/bundles")
    RestResponse<?> findAllRelatedBundles(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable String id
    );
}
