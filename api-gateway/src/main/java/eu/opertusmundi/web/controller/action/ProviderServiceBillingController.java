package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.account.EnumServiceBillingRecordSortField;
import eu.opertusmundi.common.model.payment.provider.ProviderServiceBillingCollectionDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.ServiceBillingEndPoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.ProviderServiceBilling,
    description = "The provider subscription billing API"
)
@RequestMapping(path = "/action/provider", produces = "application/json")
@Secured({"ROLE_PROVIDER"})
public interface ProviderServiceBillingController {

    /**
     * Search subscription billing records
     *
     * @param subscriptionKey
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "provider-subscription-billing-01",
        summary     = "Find All",
        description = "Search provider subscription billing records. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ProviderServiceBillingCollectionDto.class)
        )
    )
    @GetMapping(value = "/subscription-billing")
    RestResponse<?> findAll(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Subscription key"
        )
        @RequestParam(required = false) UUID subscriptionKey,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Status"
        )
        @RequestParam(name = "status", required = false) Set<EnumPayoffStatus> status,
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
        @RequestParam(defaultValue = "CREATED_ON") EnumServiceBillingRecordSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Get subscription billing details
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "provider-subscription-billing-02",
        summary     = "Find One",
        description = "Get subscription billing details. If the operation is successful, an instance of `ProviderServiceBillingResponse` "
                    + "is returned with subscription billing details; Otherwise an instance of `BaseResponse` "
                    + "is returned with one or more error messages. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ServiceBillingEndPoints.ProviderServiceBillingResponse.class)
        )
    )
    @GetMapping(value = "/subscription-billing/{key}")
    RestResponse<?> findOne(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Subscription billing record unique key"
            )
        @PathVariable UUID key
    );

    /**
     * Get subscription / private OGC service use static
     *
     * @param key
     * @param year
     * @param month
     * @return
     */
    @Operation(
        operationId = "provider-subscription-billing-03",
        summary     = "Get usage statistics",
        description = "Get subscription usage statistics. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ServiceBillingEndPoints.ServiceUseStats.class)
        )
    )
    @GetMapping(value = "/subscription-billing/{consumerKey}/{subscriptionKey}/statistics/{year}/{month}")
    RestResponse<?> getUsageStatistics(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Consumer key"
            )
        @PathVariable UUID consumerKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Subscription key"
            )
        @PathVariable UUID subscriptionKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Billing interval year"
            )
        @PathVariable Integer year,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Billing interval month"
            )
        @PathVariable Integer month
    );

}
