package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.EnumOrderSortField;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.PaymentEndPoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.OrderProvider,
    description = "The provider order API"
)
@RequestMapping(path = "/action/provider", produces = "application/json")
@Secured({"ROLE_PROVIDER"})
public interface ProviderOrderController {

    /**
     * Get Order details
     *
     * @param orderKey
     * @return A {@link RestResponse} object with a result of type
     *         {@link ProviderOrderDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "provider-order-01",
        summary     = "Get Order",
        description = "Get Order details. If the operation is successful, an instance of `OrderResponse` "
                    + "is returned with Order details; Otherwise an instance of `BaseResponse` "
                    + "is returned with one or more error messages. Roles required: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {
                BaseResponse.class, PaymentEndPoints.ProviderOrderResponse.class
            })
        )
    )
    @GetMapping(value = "/orders/{orderKey}")
    RestResponse<?> findOne(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
            )
        @PathVariable UUID orderKey
    );

    /**
     * Search provider Order records
     *
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "provider-order-02",
        summary     = "Provider Orders",
        description = "Search provider Order records. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PaymentEndPoints.ProviderOrderCollectionResponse.class)
        )
    )
    @GetMapping(value = "orders")
    RestResponse<?> findAll(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order status"
        )
        @RequestParam(name = "status", required = false) Set<EnumOrderStatus> status,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order reference number"
        )
        @RequestParam(name = "referenceNumber", required = false, defaultValue = "") String referenceNumber,
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
        @RequestParam(name = "orderBy", defaultValue = "MODIFIED_ON") EnumOrderSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

}
