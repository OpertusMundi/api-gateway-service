package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.EnumOrderSortField;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderConfirmCommandDto;
import eu.opertusmundi.common.model.order.OrderFillOutAndUploadContractCommand;
import eu.opertusmundi.common.model.order.OrderShippingCommandDto;
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
                    + "is returned with one or more error messages. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`"
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
    @Secured({"ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS"})
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
        description = "Search provider Order records. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`"
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
    @Secured({"ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS"})
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
     * Confirm order
     *
     * @param orderKey The order unique key
     * @param command The confirmation command
     * @return
     */
    @Operation(
        operationId = "provider-order-03",
        summary     = "Confirm order",
        description = "Accept or reject an order when consumer vetting is required. "
                    + "The order status must be `PENDING_PROVIDER_APPROVAL`. "
                    + "Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ProviderOrderResponse.class})
        )
    )
    @PutMapping(value = "/orders/{orderKey}")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse confirmOrder(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable UUID orderKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Confirmation command.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderConfirmCommandDto.class)),
            required = true
        )
        @RequestBody OrderConfirmCommandDto command
    );

    /**
     * Ship order
     *
     * @param orderKey The order unique key
     * @param command The shipping command
     * @return
     */
    @Operation(
        operationId = "provider-order-04",
        summary     = "Ship order",
        description = "Confirm that an order that is delivered externally from the platform has been shipped. "
                    + "The order status must be `PENDING_PROVIDER_SEND_CONFIRMATION`. "
                    + "Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ProviderOrderResponse.class})
        )
    )
    @PutMapping(value = "/orders/{orderKey}/shipping", consumes = "application/json")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse shipOrder(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable UUID orderKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Order shipping command.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderShippingCommandDto.class)),
            required = true
        )
        @RequestBody OrderShippingCommandDto command
    );
    
    /**
     * Fill out contract with the consumer's info and it
     *
     * @param orderKey The order unique key
     * @param command The fill out and upload command
     * @return
     */
    @Operation(
        operationId = "provider-order-05",
        summary     = "Fill out and upload contract",
        description = "Fill out the contract with the consumer's informations and upload it"
                    + "The order status must be `PENDING_PROVIDER_CONTRACT_FILLING_OUT`. "
                    + "Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ProviderOrderResponse.class})
        )
    )
    @PostMapping(value = "/orders/{orderKey}/uploadFilledOutContract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Secured({"ROLE_PROVIDER"})
    BaseResponse fillOutAndUploadContractForOrder(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable UUID orderKey,
        @Parameter(schema = @Schema(
            name = "file", type = "string", format = "binary", description = "Uploaded file"
        ))
        @NotNull @RequestPart(name = "file", required = true) MultipartFile file,
        @Valid @RequestPart(name = "data", required = true) OrderFillOutAndUploadContractCommand command
    );

}
