package eu.opertusmundi.web.controller.action;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.asset.EnumConsumerSubSortField;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.EnumType;
import eu.opertusmundi.common.model.openapi.schema.ConsumerEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.Consumer,
    description = "The consumer API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface ConsumerAssetController {

    /**
     * Search consumer owned assets
     *
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "consumer-assets-01",
        summary     = "Owned Assets",
        description = "Browse consumer's owned assets. Required roles: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ConsumerEndpointTypes.AssetCollectionResponse.class)
        )
    )
    @GetMapping(value = "/consumer/assets")
    @Secured({"ROLE_CONSUMER"})
    RestResponse<?> findAllAssets(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Asset type"
        )
        @RequestParam(name = "type", required = false) EnumType type,
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
        @RequestParam(name = "orderBy", defaultValue = "ADDED_ON") EnumConsumerAssetSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Search consumer registered subscriptions
     *
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "consumer-assets-02",
        summary     = "Registered subscriptions",
        description = "Browse consumer's registered subscriptions. Required roles: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ConsumerEndpointTypes.AssetSubscriptionResponse.class)
        )
    )
    @GetMapping(value = "/consumer/subscriptions")
    @Secured({"ROLE_CONSUMER"})
    RestResponse<?> findAllSubscriptions(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Asset type"
        )
        @RequestParam(name = "type", required = false) EnumSpatialDataServiceType type,
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
        @RequestParam(name = "orderBy", defaultValue = "ADDED_ON") EnumConsumerSubSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

}
