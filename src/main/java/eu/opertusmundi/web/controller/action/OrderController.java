package eu.opertusmundi.web.controller.action;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientItemResponse;
import eu.opertusmundi.web.model.order.OrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for managing orders
 */
@Tag(
    name        = "Order",
    description = "The order API"
)
@RequestMapping(path = "/action", produces = "application/json")
@Secured({ "ROLE_USER" })
public interface OrderController {

    /**
     * Create a new order
     *
     * @param order The order to create
     * @return A {@link RestResponse} with a result of type {@link OrderDto}
     */
    @Operation(
        operationId = "order-01",
        summary     = "Create a new order",
        tags        = { "Order" },
        hidden      = true
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueClientItemResponse.class))
    )
    @PostMapping(value = "/orders", consumes = "application/json")
    RestResponse<?> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "An order DTO.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class)),
            required = true
        )
        @RequestBody OrderDto order
    );

}
