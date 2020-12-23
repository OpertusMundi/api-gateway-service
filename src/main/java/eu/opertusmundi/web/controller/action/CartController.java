package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.openapi.schema.CartEndpointTypes;
import eu.opertusmundi.web.model.order.CartAddCommandDto;
import eu.opertusmundi.web.model.order.CartDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for managing user shopping cart
 */
@Tag(
    name        = "Cart",
    description = "The cart API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface CartController {

    /**
     * Get cart
     *
     * @return An instance of {@link CartEndpointTypes.CartResponse}
     */
    @Operation(
        operationId = "cart-01",
        summary     = "Get cart",
        description = "Get shopping cart for the current user session",
        tags        = { "Cart" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartEndpointTypes.CartResponse.class))
    )
    @GetMapping(value = "/cart")
    RestResponse<CartDto> getCart(@Parameter(hidden = true) HttpSession session);

    /**
     * Add item to cart
     *
     * @param command The command object for adding an item to the cart
     *
     * @return An instance of {@link CartEndpointTypes.CartResponse}
     */
    @Operation(
        operationId = "cart-02",
        summary     = "Add item",
        description = "Add item to cart",
        tags        = { "Cart" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartEndpointTypes.CartResponse.class))
    )
    @PostMapping(value = "/cart")
    RestResponse<CartDto> addItem(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Item to add",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartAddCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) CartAddCommandDto command,
        @Parameter(hidden = true) HttpSession session
    );

    @Operation(
        operationId = "cart-03",
        summary     = "Remove item",
        description = "Remove item from cart",
        tags        = { "Cart" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartEndpointTypes.CartResponse.class))
    )
    @DeleteMapping(value = "/cart/{id}")
    RestResponse<CartDto> removeItem(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Cart item unique id"
        )
        @PathVariable(name = "id", required = true) UUID itemId,
        @Parameter(hidden = true) HttpSession session
    );

    @Operation(
        operationId = "cart-04",
        summary     = "Clear cart",
        description = "Remove all items from the cart",
        tags        = { "Cart" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CartEndpointTypes.CartResponse.class))
    )
    @DeleteMapping(value = "/cart")
    RestResponse<CartDto> clear(@Parameter(hidden = true) HttpSession session);

}
