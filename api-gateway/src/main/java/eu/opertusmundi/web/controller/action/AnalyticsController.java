package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.analytics.AssetQuery;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.SalesQuery;
import eu.opertusmundi.common.model.openapi.schema.AnalyticsEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.Analytics,
    description = "Data analysis API"
)
@RequestMapping(path = "/action/analytics", produces = "application/json")
public interface AnalyticsController {

    /**
     * Query asset sales data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-01",
        summary     = "Sales",
        description = "Execute a query on sales data and return a single data series. Required roles: <b>ROLE_PROVIDER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(
                implementation = AnalyticsEndpointTypes.BigDecimalDataSeries.class
            )
        )
    )
    @PostMapping(value = "/sales", consumes = { "application/json" })
    @Secured({ "ROLE_PROVIDER" })
    @Validated
    RestResponse<?> executeSalesQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SalesQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        SalesQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Query asset views
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-02",
        summary     = "Assets",
        description = "Execute a query on asset views and return a single data series. Required roles: <b>ROLE_PROVIDER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(
                implementation = AnalyticsEndpointTypes.BigDecimalDataSeries.class
            )
        )
    )
    @PostMapping(value = "/assets", consumes = { "application/json" })
    @Secured({ "ROLE_PROVIDER" })
    @Validated
    RestResponse<?> executeAssetQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AssetQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

}