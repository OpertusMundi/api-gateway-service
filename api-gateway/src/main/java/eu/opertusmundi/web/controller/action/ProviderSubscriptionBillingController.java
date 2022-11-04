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
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingSortField;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingStatus;
import eu.opertusmundi.common.model.payment.provider.ProviderSubscriptionBillingCollectionDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.SubscriptionBillingEndPoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.ProviderSubscriptionBilling,
    description = "The provider subscription billing API"
)
@RequestMapping(path = "/action/provider", produces = "application/json")
@Secured({"ROLE_PROVIDER"})
public interface ProviderSubscriptionBillingController {

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
            mediaType = "application/json", schema = @Schema(implementation = ProviderSubscriptionBillingCollectionDto.class)
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
        @RequestParam(name = "status", required = false) Set<EnumSubscriptionBillingStatus> status,
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
        @RequestParam(defaultValue = "CREATED_ON") EnumSubscriptionBillingSortField orderBy,
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
        description = "Get subscription billing details. If the operation is successful, an instance of `ProviderSubscriptionBillingResponse` "
                    + "is returned with subscription billing details; Otherwise an instance of `BaseResponse` "
                    + "is returned with one or more error messages. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SubscriptionBillingEndPoints.ProviderSubscriptionBillingResponse.class)
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

}
