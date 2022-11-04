package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingSortField;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingStatus;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommandDto;
import eu.opertusmundi.common.model.payment.CheckoutSubscriptionBillingCommandDto;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerSubscriptionBillingCollectionDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.PaymentEndPoints;
import eu.opertusmundi.web.model.openapi.schema.SubscriptionBillingEndPoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.ConsumerSubscriptionBilling,
    description = "The consumer subscription billing API"
)
@RequestMapping(path = "/action/consumer", produces = "application/json")
@Secured({"ROLE_CONSUMER"})
public interface ConsumerSubscriptionBillingController {

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
        operationId = "consumer-subscription-billing-01",
        summary     = "Find All",
        description = "Search consumer subscription billing records. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ConsumerSubscriptionBillingCollectionDto.class)
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
        operationId = "consumer-subscription-billing-02",
        summary     = "Find One",
        description = "Get subscription billing details. If the operation is successful, an instance of `ConsumerSubscriptionBillingResponse` "
                    + "is returned with subscription billing details; Otherwise an instance of `BaseResponse` "
                    + "is returned with one or more error messages. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = SubscriptionBillingEndPoints.ConsumerSubscriptionBillingResponse.class)
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
     * Creates a new PayIn for a list of subscription billing record keys
     *
     * @return A {@link RestResponse} object with a result of type
     *         {@link PayInDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "consumer-subscription-billing-03",
        summary     = "Checkout",
        description = "Create a new PayIn for a list of subscription billing record keys. If the operation "
                    + "is successful, an instance of `CheckoutPayInResponse` is returned with the new PayIn; "
                    + "Otherwise an instance of `BaseResponse` is returned with one or more error messages. "
                    + "Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.CheckoutPayInResponse.class})
        )
    )
    @PostMapping(value = "/subscription-billing/checkout")
    RestResponse<?> checkout(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description =
                "Card direct PayIn command. "
              + "<ul>"
              + "<li> If both the `billing` and `shipping` properties are empty, the information from the consumer registration is sent to the issuer.</li>"
              + "<li> If the `billing` is supplied but the `shipping` is empty, the `shipping` property is initialized with the fields supplied for `billing`.</li>"
              + "<li> If the `shipping` is supplied but the `billing` is empty, the `billing` property is initialized with fields supplied for `shipping`.</li>"
              + "</ul>",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardDirectPayInCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        CheckoutSubscriptionBillingCommandDto command
    );

}
