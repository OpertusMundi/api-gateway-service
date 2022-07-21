package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.analytics.AssetCountQuery;
import eu.opertusmundi.common.model.analytics.AssetTotalValueQuery;
import eu.opertusmundi.common.model.analytics.AssetTypeEarningsQuery;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.CoverageQuery;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.GoogleAnalyticsQuery;
import eu.opertusmundi.common.model.analytics.SalesQuery;
import eu.opertusmundi.common.model.analytics.SubscribersQuery;
import eu.opertusmundi.common.model.analytics.VendorCountQuery;
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
    @Secured({ "ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS" })
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
    @Secured({ "ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS" })
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
        description = "Execute a query on coverage data and return a single data series",
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
                schema = @Schema(implementation = CoverageQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        CoverageQuery query,
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
        summary     = "Asset Total Value",
        description = "Execute a query on published asset pricing model data and return a single data series with asset total value",
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
    @PostMapping(value = "/asset-total-value", consumes = { "application/json" })
    @Validated
    RestResponse<?> executeTotalAssetValueQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetTotalValueQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AssetTotalValueQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Query asset count data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-05",
        summary     = "Asset Count",
        description = "Execute a query on published assets data and return a single data series with assets count",
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
    @PostMapping(value = "/asset-count", consumes = { "application/json" })
    @Validated
    RestResponse<?> executeAssetCountQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetCountQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AssetCountQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Query aggregated asset views data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link List<ImmutablePair<String, Integer>>} result
     */
    @Operation(
        operationId = "analytics-06",
        summary     = "Most Popular Assets",
        description = "Execute a query on aggregated asset views data and return the most popular views or searches",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(
                implementation = AnalyticsEndpointTypes.ListOfImmutablePairs.class
            )
        )
    )
    @PostMapping(value = "/popular-assets", consumes = { "application/json" })
    @Validated
    RestResponse<?> executeFindPopularAssetViewsAndSearches(
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
     * Query asset views data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link List<ImmutablePair<String, Integer>>} result
     */
    @Operation(
        operationId = "analytics-07",
        summary     = "Most Popular Terms",
        description = "Execute a query on asset views data and return the most popular terms",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(
                implementation = AnalyticsEndpointTypes.ListOfImmutablePairs.class
            )
        )
    )
    @RequestMapping(value = "/popular-terms", method=RequestMethod.GET)
    @Validated
    RestResponse<?> executeFindPopularTerms(
    );
    
    /**
     * Query account data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-08",
        summary     = "Vendor Count",
        description = "Execute a query on accounts and return a single data series with count of vendors",
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
    @PostMapping(value = "/vendor-count", consumes = { "application/json" })
    @Validated
    RestResponse<?> executeVendorCountQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VendorCountQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        VendorCountQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Query subscribers data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-09",
        summary     = "Subscribers",
        description = "Execute a query on subscribers data and return a single data series. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`",
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
    @PostMapping(value = "/subscribers", consumes = { "application/json" })
//    @Secured({ "ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS" })
    @Validated
    RestResponse<?> executeSubscribersQuery(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscribersQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        SubscribersQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Query Google Analytics data
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-10",
        summary     = "Google Analytics",
        description = "Execute a query on google analytics data and return a single data series. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`",
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
    @PostMapping(value = "/google-analytics", consumes = { "application/json" })
//    @Secured({ "ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS" })
    @Validated
    RestResponse<?> executeGoogleAnalytics(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GoogleAnalyticsQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        GoogleAnalyticsQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Query earnings per asset type
     *
     * @param request The query to execute
     *
     * @return A {@link RestResponse} with a {@link DataSeries} result
     */
    @Operation(
        operationId = "analytics-11",
        summary     = "Earnings per Asset type",
        description = "Execute a query on sales data and return a single data series per asset type. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`",
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
    @PostMapping(value = "/earnings-asset-type", consumes = { "application/json" })
//    @Secured({ "ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS" })
    @Validated
    RestResponse<?> executeEarningsPerAssetType(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Query to execute",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssetTypeEarningsQuery.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        AssetTypeEarningsQuery query,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

}