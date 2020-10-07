package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.order.CreatePaymentResponse;
import eu.opertusmundi.web.model.order.CreatePaymentResult;
import eu.opertusmundi.web.model.order.OrderDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for accepting payments
 */
@Tag(
    name        = "Payment",
    description = "The payment API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface PaymentController {

    /**
     * Prepare a new payment intent.
     *
     * @param id The order unique id
     * @param order The order associated to the payment intent
     * @return A {@link RestResponse} with a result of type {@link CreatePaymentResult}
     */
    @Operation(
        summary     = "Create a payment intent",
        description = "Creates a payment intent using the Stripe payment API.",
        tags        = { "Payment" },
        hidden      = true
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreatePaymentResponse.class))
    )
    @PostMapping(value = "/payments/{id}", consumes = "application/json")
    @Secured({ "ROLE_USER" })
    RestResponse<?> create(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "The order unique id"
        )
        @PathVariable UUID id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "The order associated to the payment intent. Cannot be null or empty.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class)),
            required = true
        )
        @RequestBody OrderDto order
    );

    /**
     * Payment service webhook endpoint
     *
     * @param payload Payment service event in JSON format
     * @return An empty {@link RestResponse} instance.
     */
    @Hidden
    @PostMapping(value = "/payments/stripe/webhook", consumes = "application/json")
    RestResponse<?> handleEvent(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload);

}
