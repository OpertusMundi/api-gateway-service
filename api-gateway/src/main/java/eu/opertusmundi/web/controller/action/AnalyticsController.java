package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.BaseQuery;
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
@Secured({ "ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS" })
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
        description = "Execute a query on sales data and return a single data series. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`",
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
        description = "Execute a query on asset views and return a single data series. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`",
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
    @Validated
    RestResponse<?> executeAssetQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetViewQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AssetViewQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Query asset coverage data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-03",
        summary     = "Coverage",
        description = "Execute a query on coverage data and return a single data series. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`",
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
    @PostMapping(value = "/coverage", consumes = { "application/json" })
    @Validated
    RestResponse<?> executeCoverageQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        BaseQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Query asset price data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-04",
        summary     = "Total Price",
        description = "Execute a query on pricing data and return a single data series. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`",
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
    @PostMapping(value = "/price", consumes = { "application/json" })
    @Validated
    RestResponse<?> executePriceQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        BaseQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

}