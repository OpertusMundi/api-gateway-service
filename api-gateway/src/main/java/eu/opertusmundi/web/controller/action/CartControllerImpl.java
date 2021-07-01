package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.order.CartAddCommandDto;
import eu.opertusmundi.common.model.order.CartConstants;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.service.CartService;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.PaymentService;
import eu.opertusmundi.common.service.QuotationService;

@RestController
public class CartControllerImpl extends BaseController implements CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartControllerImpl.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private PaymentService paymentService;

    @Override
    public RestResponse<CartDto> getCart(HttpSession session) {
        final UUID cartId = (UUID) session.getAttribute(CartConstants.CART_SESSION_KEY);

        final CartDto cart = this.cartService.getCart(cartId);

        this.updateCart(session, cart);

        return RestResponse.result(cart);
    }

    @Override
    public RestResponse<CartDto> addItem(CartAddCommandDto command, BindingResult validationResult, HttpSession session) {
        final UUID cartKey = (UUID) session.getAttribute(CartConstants.CART_SESSION_KEY);
        command.setCartKey(cartKey);

        final CartDto cart = this.cartService.addItem(command);

        this.updateCart(session, cart);

        return RestResponse.result(cart);
    }

    @Override
    public RestResponse<CartDto> removeItem(UUID itemId, HttpSession session) {
        final UUID cartId = (UUID) session.getAttribute(CartConstants.CART_SESSION_KEY);

        final CartDto cart = this.cartService.removeItem(cartId, itemId);

        this.updateCart(session, cart);

        return RestResponse.result(cart);
    }

    @Override
    public RestResponse<CartDto> clear(HttpSession session) {
        final UUID cartId = (UUID) session.getAttribute(CartConstants.CART_SESSION_KEY);

        final CartDto cart = this.cartService.clear(cartId);

        this.updateCart(session, cart);

        return RestResponse.result(cart);
    }

    @Override
    public RestResponse<?> checkout(HttpSession session) {
        // Get current cart
        final UUID    cartKey = (UUID) session.getAttribute(CartConstants.CART_SESSION_KEY);
        CartDto cart    = this.cartService.getCart(cartKey);

        // Link authenticated user to the cart
        if (cart.getAccountId() == null && this.currentUserId() != null) {
            cart = this.cartService.setAccount(cart.getKey(), this.currentUserId());
        }

        // Create order
        final OrderDto order = this.paymentService.createOrderFromCart(cart, this.getLocation());

        return RestResponse.result(order);
    }

    private void updateCart(HttpSession session, CartDto cart) {
        try {
            // Update cart key in session
            session.setAttribute(CartConstants.CART_SESSION_KEY, cart.getKey());
            // Link authenticated user to the cart
            if (cart.getAccountId() == null && this.currentUserId() != null) {
                this.cartService.setAccount(cart.getKey(), this.currentUserId());
            }

            // Inject catalogue data
            final String[] keys = cart.getItems().stream().map(i -> i.getAssetId()).toArray(String[]::new);

            if (keys.length != 0) {
                final List<CatalogueItemDto> result = this.catalogueService.findAllById(keys);

                final List<CatalogueItemDto> catalogueItems = result.stream()
                    .map(item -> {
                        // Do not return metadata/ingestion information
                        item.setAutomatedMetadata(null);
                        item.setIngestionInfo(null);

                        return item;
                    }).collect(Collectors.toList());

                // Set product and selected pricing model
                cart.getItems().stream().forEach(cartItem -> {
                    final CatalogueItemDto catalogueItem = catalogueItems.stream()
                        .filter(i -> i.getId().equals(cartItem.getAssetId()))
                        .findFirst()
                        .orElse(null);

                    cartItem.setAsset(catalogueItem);

                    final EffectivePricingModelDto pricingModel = quotationService.createQuotation(
                        catalogueItem, cartItem.getPricingModelKey(), cartItem.getQuotationParameters()
                    );

                    cartItem.setPricingModel(pricingModel);
                });
            }
        } catch (final QuotationException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Failed to update cart", ex);

            throw new ServiceException(BasicMessageCode.InternalServerError, "Cart operation failed");
        }
    }

}