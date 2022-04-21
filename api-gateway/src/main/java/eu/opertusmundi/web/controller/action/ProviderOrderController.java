package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.EnumOrderSortField;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderConfirmCommandDto;
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
    @PutMapping(value = "/orders/{orderKey}/confirmation")
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
     * Upload order custom contract
     *
     * @param orderKey The order unique key
     * @param command The fill out and upload command
     * @return
     */
    @Operation(
        operationId = "provider-order-05",
        summary     = "Upload order contract",
        description = "Uploads order custom provider contract with the consumer's information. The order status is not updated. "
                    + "The order status must be `PENDING_PROVIDER_CONTRACT_UPLOAD`. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ProviderOrderResponse.class})
        )
    )
    @PutMapping(value = "/orders/{orderKey}/contract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Secured({"ROLE_PROVIDER"})
    BaseResponse uploadOrderContract(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable UUID orderKey,
        @Parameter(schema = @Schema(
            name = "file", type = "string", format = "binary", description = "Uploaded file"
        ))
        @NotNull @RequestPart(name = "file", required = true) MultipartFile file
    );

    /**
     * Upload order custom contract
     *
     * @param orderKey The order unique key
     * @param command The fill out and upload command
     * @return
     */
    @Operation(
        operationId = "provider-order-06",
        summary     = "Upload order contract",
        description = "Uploads order custom provider contract with the consumer's information. The order status is updated. "
                    + "The contract cannot be updated after this call. The order status must be `PENDING_PROVIDER_CONTRACT_UPLOAD`. "
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
    @PostMapping(value = "/orders/{orderKey}/contract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Secured({"ROLE_PROVIDER"})
    BaseResponse uploadOrderContractAndSubmit(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable UUID orderKey,
        @Parameter(schema = @Schema(
            name = "file", type = "string", format = "binary", description = "Uploaded file"
        ))
        @NotNull @RequestPart(name = "file", required = true) MultipartFile file
    );

    /**
     * Download a contract  file
     *
     * @param pid Asset persistent identifier (PID)
     *
     * @return The requested file
     */
    @Operation(
        operationId = "assets-07",
        summary     = "Download contract file",
        description = "Downloads custom provider contract for the specified order"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/orders/{orderKey}/contract", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<StreamingResponseBody> downloadContract(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable UUID orderKey,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

}
