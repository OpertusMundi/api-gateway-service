package eu.opertusmundi.web.controller.integration;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.openapi.schema.SentinelHubEndpointTypes;
import eu.opertusmundi.common.model.openapi.schema.SentinelHubEndpointTypes.SentinelHubCatalogueResponse;
import eu.opertusmundi.common.model.openapi.schema.SentinelHubEndpointTypes.SentinelHubOpenDataCollectionResponse;
import eu.opertusmundi.common.model.openapi.schema.SentinelHubEndpointTypes.SentinelHubSubscriptionResponse;
import eu.opertusmundi.common.model.sinergise.client.ClientCatalogueQueryDto;
import eu.opertusmundi.web.model.openapi.schema.CommonTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for accessing Sentinel Hub open data collections
 */
@Tag(name = EndpointTags.SentinelHub)
@RequestMapping(path = "/action/integration/sentinel-hub", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "cookie")
public interface SentinelHubController {

    /**
     * Search satellite images from Sentinel Hub using the Catalogue API
     *
     * @param query The search criteria
     * @return An instance of {@link SentinelHubCatalogueResponse} class
     */
    @Operation(
        operationId = "integration-sentinel-hub-01",
        summary     = "Search",
        description = "Search satellite images from Sentinel Hub using the Catalogue API. Required role: `ROLE_USER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = SentinelHubEndpointTypes.SentinelHubCatalogueResponse.class)
        )
    )
    @PostMapping(value = "/catalogue/search")
    @Secured({ "ROLE_USER" })
    RestResponse<?> search(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClientCatalogueQueryDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        ClientCatalogueQueryDto query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Get available subscription plans
     *
     * @return An instance of {@link SentinelHubSubscriptionResponse} class
     */
    @Operation(
        operationId = "integration-sentinel-hub-02",
        summary     = "Subscription Plans",
        description = "Get all available subscription plans for open data collections. Required role: `ROLE_USER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = SentinelHubEndpointTypes.SentinelHubSubscriptionResponse.class)
        )
    )
    @GetMapping(value = "/quotation/subscriptions")
    @Secured({ "ROLE_USER" })
    RestResponse<?> getSubscriptionPlans();

    /**
     * Check if the authenticated user has already an active subscription to
     * Sentinel Hub service
     *
     * @return An instance of {@link CommonTypes.BooleanResponse} class
     */
    @Operation(
        operationId = "integration-sentinel-hub-03",
        summary     = "Check subscription",
        description = "Check if the authenticated user has already an active subscription to Sentinel Hub service. Required role: `ROLE_USER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CommonTypes.BooleanResponse.class)
        )
    )
    @GetMapping(value = "/subscriptions")
    @Secured({ "ROLE_USER" })
    RestResponse<Boolean> isSubscribed();

    /**
     * Get supported open data collections
     *
     * @return An instance of {@link SentinelHubOpenDataCollectionResponse} class
     */
    @Operation(
        operationId = "integration-sentinel-hub-04",
        summary     = "Open Data Collections",
        description = "Get all supported open data collections"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = SentinelHubEndpointTypes.SentinelHubOpenDataCollectionResponse.class)
        )
    )
    @GetMapping(value = "/open-data/collections")
    RestResponse<?> getOpenDataCollections();

}
