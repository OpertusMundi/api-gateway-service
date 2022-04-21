package eu.opertusmundi.web.controller.action;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.EnumProviderAssetSortField;
import eu.opertusmundi.common.model.asset.EnumProviderSubSortField;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
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
    name        = EndpointTags.ProviderAssets,
    description = "API for browsing provider published assets and active subscriptions"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface ProviderAssetController {

    /**
     * Search catalogue items published by the provider
     *
     * @param query
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "assets-01",
        summary     = "Published assets",
        description = "Search catalogue for provider's published assets. "
                    + "Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.ItemCollectionResponse.class)
        )
    )
    @GetMapping(value = "/assets")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findAllAssets(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Query"
        )
        @RequestParam(name = "q", required = true) String query,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Asset type"
        )
        @RequestParam(name = "type", required = false) EnumAssetType type,
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
        @RequestParam(name = "orderBy", defaultValue = "TITLE") EnumProviderAssetSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Search the registered subscriptions of provider
     *
     * @param query
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "assets-02",
        summary     = "Registered subscriptions",
        description = "Search the registered subscriptions of provider. "
                    + "Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.SubCollectionResponse.class)
        )
    )
    @GetMapping(value = "/subscriptions")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findAllSubscriptions(
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
        @RequestParam(name = "orderBy", defaultValue = "TITLE") EnumProviderSubSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Download an additional resource file
     *
     * @param pid Asset persistent identifier (PID)
     * @param resourceKey Resource unique key
     *
     * @return The requested file
     */
    @Operation(
        operationId = "assets-03",
        summary     = "Download additional resource",
        description = "Downloads an additional resource file"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/assets/{pid}/additional-resources/{resourceKey}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset pid"
        )
        @PathVariable String pid,
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
     * @param pid Asset persistent identifier (PID)
     * @param resourceKey Resource unique key
     * @param propertyName The property name
     *
     * @return The requested property value
     */
    @Operation(
        operationId = "assets-04",
        summary     = "Get metadata property",
        description = "Gets metadata property value for the specified resource file. Required role: `ROLE_USER`",
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
        value = "/assets/{pid}/resources/{resourceKey}/metadata/{propertyName}",
        produces = {MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    ResponseEntity<StreamingResponseBody> getMetadataProperty(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset pid"
        )
        @PathVariable String pid,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
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
     * Download a contract  file
     *
     * @param pid Asset persistent identifier (PID)
     *
     * @return The requested file
     */
    @Operation(
        operationId = "assets-05",
        summary     = "Download contract file",
        description = "Downloads a provider uploaded contract file"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/assets/{pid}/contract", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<StreamingResponseBody> downloadContract(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset pid"
        )
        @PathVariable String pid,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

    /**
     * Download a contract annex file
     *
     * @param pid Asset persistent identifier (PID)
     * @param annexKey Contract annex unique key
     *
     * @return The requested file
     */
    @Operation(
        operationId = "assets-06",
        summary     = "Download contract annex",
        description = "Downloads a contract annex file"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/assets/{pid}/contract/annexes/{annexKey}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<StreamingResponseBody> downloadContractAnnex(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset pid"
        )
        @PathVariable String pid,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract annex unique key"
        )
        @PathVariable String annexKey,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

    /**
     * Delete catalogue item
     *
     * @param id The item unique id
     * @return
     */
    @Operation(
        operationId = "assets-07",
        summary     = "Delete asset",
        description = "Delete asset from catalogue. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/assets/{id}")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse unpublishAsset(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable String id
    );

}
