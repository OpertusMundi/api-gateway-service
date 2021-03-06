package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommandDto;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.EnumPayInSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.PaymentEndPoints;
import eu.opertusmundi.web.model.openapi.schema.PaymentEndPoints.CardCollectionResponse;
import eu.opertusmundi.web.model.openapi.schema.PaymentEndPoints.CardRegistrationRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.PayInConsumer,
    description = "The consumer PayIn API"
)
@RequestMapping(path = "/action/consumer", produces = "application/json")
@Secured({"ROLE_CONSUMER"})
public interface ConsumerPayInController {

    /**
     * Creates a bankwire PayIn for a specific order
     *
     * @param orderKey
     * @param session
     *
     * @return A {@link RestResponse} object with a result of type
     *         {@link BankwirePayInDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "consumer-payin-01",
        summary     = "Create Bankwire PayIn",
        description = "Create a new bankwire PayIn for the order with the given key. If the operation "
                    + "is successful, an instance of `ConsumerBankWirePayInResponse` is returned with PayIn details; "
                    + "Otherwise an instance of `BaseResponse` is returned with one or more error messages. "
                    + "Moreover, on successful execution, the server resets the user cart; Hence, the client "
                    + "must either reset the cart locally or invoke [Get Cart](#operation/cart-01) "
                    + "should refresh"
                    + "Roles required: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ConsumerBankWirePayInResponse.class})
        )
    )
    @PostMapping(value = "/payins/bankwire/{orderKey}")
    RestResponse<?> createBankwirePayIn(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
            )
        @PathVariable UUID orderKey,
        @Parameter(hidden = true) HttpSession session
    );

    /**
     * Get user registered cards
     *
     * @return A {@link RestResponse} object with a result of type
     *         {@link CardCollectionResponse} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "consumer-payin-02",
        summary     = "Get Cards",
        description = "Get all user registered cards. If operation is successful, an instance of "
                    + "`CardCollectionResponse` is returned with the user's cards; Otherwise "
                    + "an instance of `BaseResponse` is returned with one or more error messages. "
                    + "Roles required: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.CardCollectionResponse.class})
        )
    )
    @GetMapping(value = "/cards")
    RestResponse<?> getCards(
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page index (0-based)"
        )
        @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page size"
        )
        @RequestParam(name = "size", required = false, defaultValue = "10") Integer size
    );

    /**
     * Initialize a new card registration
     *
     * @return A {@link RestResponse} object with a result of type
     *         {@link CardRegistrationRequestResponse} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "consumer-payin-03",
        summary     = "Initialize Card Registration",
        description = "Initializes a card registration. If operation is successful, an instance of "
                    + "`CardRegistrationRequestResponse` is returned with the card registration data; "
                    + "Otherwise an instance of `BaseResponse` is returned with one or more error messages."
                    + "<br/>"
                    + "Client must post card details to `cardRegistrationUrl` as explained in [MANGOPAY Documentation]"
                    + "(https://docs.mangopay.com/endpoints/v2.01/cards#e1042_post-card-info) page. "
                    + "Roles required: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.CardRegistrationRequestResponse.class})
        )
    )
    @PostMapping(value = "/cards")
    RestResponse<?> createCardRegistration();

    /**
     * Completes card registration
     *
     * @return A {@link RestResponse} object with a result of type
     *         {@link CardDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "consumer-payin-04",
        summary     = "Complete Card Registration",
        description = "Completes the registration of a new card. If operation is successful, an instance of "
                    + "`CardRegistrationResponse` is returned with the new card information; "
                    + "Otherwise an instance of `BaseResponse` is returned with one or more error messages. "
                    + "Roles required: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.CardRegistrationResponse.class})
        )
    )
    @PutMapping(value = "/cards", consumes = "application/json")
    @Validated
    RestResponse<?> completeCardRegistration(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Card registration command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardRegistrationCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        CardRegistrationCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Creates a card direct PayIn for a specific order
     *
     * @param orderKey
     * @param command
     * @param validationResult
     * @param session
     *
     * @return A {@link RestResponse} object with a result of type
     *         {@link CardDirectPayInDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "consumer-payin-05",
        summary     = "Create Card Direct PayIn",
        description = "Create a new card direct PayIn for the order with the given key. If the operation "
                    + "is successful, an instance of `ConsumerCardDirectPayInResponse` is returned with PayIn details; "
                    + "Otherwise an instance of `BaseResponse` is returned with one or more error messages. "
                    + "Moreover, on successful execution, the server resets the user cart; Hence, the client "
                    + "must either reset the cart locally or invoke [Get Cart](#operation/cart-01). If 3-D Secure "
                    + "validation is required or the operation fails, the cart is not updated. "
                    + "Roles required: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ConsumerCardDirectPayInResponse.class})
        )
    )
    @PostMapping(value = "/payins/card-direct/{orderKey}", consumes = "application/json")
    @Validated
    RestResponse<?> createCardDirectPayIn(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
            )
        @PathVariable UUID orderKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Card direct PayIn command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CardDirectPayInCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        CardDirectPayInCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult,
        @Parameter(hidden = true) HttpSession session
    );

    /**
     * Get PayIn details
     *
     * @param payInKey
     * @return A {@link RestResponse} object with a result of type
     *         {@link PayInDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "consumer-payin-06",
        summary     = "Get PayIn",
        description = "Get PayIn details. If the operation is successful, an instance of either `ConsumerBankWirePayInResponse` "
                    + "or `ConsumerCardDirectPayInResponse` is returned with PayIn details; Otherwise an instance of `BaseResponse` "
                    + "is returned with one or more error messages. Roles required: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {
                BaseResponse.class,
                PaymentEndPoints.ConsumerBankWirePayInResponse.class,
                PaymentEndPoints.ConsumerCardDirectPayInResponse.class
            })
        )
    )
    @GetMapping(value = "/payins/{payInKey}")
    RestResponse<?> findOnePayIn(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "PayIn unique key"
            )
        @PathVariable UUID payInKey
    );

    /**
     * Search consumer PayIn records
     *
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "consumer-payin-07",
        summary     = "Consumer PayIns",
        description = "Search consumer PayIn records. Required roles: <b>ROLE_CONSUMER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = PaymentEndPoints.ConsumerPayInCollectionResponse.class)
        )
    )
    @GetMapping(value = "/payins")
    RestResponse<?> findAllConsumerPayIns(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "PayIn status"
        )
        @RequestParam(name = "status", required = false) EnumTransactionStatus status,
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
        @RequestParam(name = "orderBy", defaultValue = "EXECUTED_ON") EnumPayInSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

}
