package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.EnumOrderSortField;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.PaymentEndPoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.OrderConsumer,
    description = "The consumer order API"
)
@RequestMapping(path = "/action/consumer", produces = "application/json")
@Secured({"ROLE_CONSUMER"})
public interface ConsumerOrderController {

    /**
     * Get Order details
     *
     * @param orderKey
     * @return A {@link RestResponse} object with a result of type
     *         {@link ConsumerOrderDTo} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "consumer-order-01",
        summary     = "Get Order",
        description = "Get Order details. If the operation is successful, an instance of `OrderResponse` "
                    + "is returned with Order details; Otherwise an instance of `BaseResponse` "
                    + "is returned with one or more error messages. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {
                BaseResponse.class, PaymentEndPoints.ConsumerOrderResponse.class
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
     * Search consumer order records
     *
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "consumer-order-02",
        summary     = "Consumer Orders",
        description = "Search consumer Order records. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PaymentEndPoints.ConsumerOrderCollectionResponse.class)
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

    /**
     * Confirm order delivery
     *
     * @param orderKey The order unique key
     * @param command The delivery command
     * @return
     */
    @Operation(
        operationId = "consumer-order-03",
        summary     = "Confirm order delivery",
        description = "Confirm that an order that is shipped externally from the platform has been delivered. "
                    + "The order status must be `PENDING_CONSUMER_RECEIVE_CONFIRMATION`. "
                    + "Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ConsumerOrderResponse.class})
        )
    )
    @PutMapping(value = "/orders/{orderKey}/confirm-delivery")
    BaseResponse confirmDelivery(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable UUID orderKey
    );

    /**
     * Accept contract
     *
     * @param orderKey The order unique key
     * @param command The accept contract command
     * @return
     */
    @Operation(
        operationId = "consumer-order-04",
        summary     = "Accept custom contract",
        description = "Accept the provider's custom contract with the consumer's information. "
                    + "The order status must be `PENDING_CONSUMER_CONTRACT_ACCEPTANCE`. "
                    + "Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ConsumerOrderResponse.class})
        )
    )
    @PutMapping(value = "/orders/{orderKey}/accept-contract")
    BaseResponse acceptContract(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable UUID orderKey
    );

    /**
     * Download invoice
     *
     * @param orderKey
     * @param response
     * @return
     */
    @Operation(
        operationId = "consumer-order-05",
        summary     = "Download Invoice",
        description = "Downloads the invoice for the specified order. Required role: `ROLE_CONSUMER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested invoice file"))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Invoice not found"
    )
    @GetMapping(value = "/orders/{key}/invoice", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Validated
    ResponseEntity<StreamingResponseBody> downloadInvoice(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable(name = "key", required = true) UUID orderKey,
        @Parameter(hidden = true)
        HttpServletResponse response
    );

}
