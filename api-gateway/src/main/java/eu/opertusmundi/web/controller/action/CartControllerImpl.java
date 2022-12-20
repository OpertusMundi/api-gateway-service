package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.order.CartAddCommandDto;
import eu.opertusmundi.common.model.order.CartConstants;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EmptyQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.service.CartService;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.QuotationService;
import eu.opertusmundi.common.service.mangopay.PaymentService;

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
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final UUID cartKey = (UUID) session.getAttribute(CartConstants.CART_SESSION_KEY);
        if (command.getParameters() == null) {
            command.setParameters(new EmptyQuotationParametersDto());
        }
        command.setCartKey(cartKey);
        command.getParameters().setUserName(this.currentUserEmail());

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
        final UUID cartKey = (UUID) session.getAttribute(CartConstants.CART_SESSION_KEY);
        CartDto    cart    = this.cartService.getCart(cartKey);

        // Link authenticated user to the cart
        if (cart.getAccountId() == null && this.currentUserId() != null) {
            cart = this.cartService.setAccount(cart.getKey(), this.currentUserId());
        }

        // Set user name to user parameters
        cart.getItems().stream().forEach(i -> {
            if (i.getQuotationParameters() == null) {
                i.getPricingModel().setUserParameters(new EmptyQuotationParametersDto());
            }
            i.getQuotationParameters().setUserName(this.currentUserEmail());
        });

        // Create order
        final OrderDto order = this.paymentService.createOrderFromCart(cart, this.getLocation());

        // Empty user's cart if either order vetting is required or a
        // custom contract must be signed
        if (order.getStatus() == EnumOrderStatus.PENDING_PROVIDER_APPROVAL ||
            order.getStatus() == EnumOrderStatus.PENDING_PROVIDER_CONTRACT_UPLOAD
        ) {
            this.cartService.clear(cartKey);
        }

        return RestResponse.result(order);
    }

    private void updateCart(HttpSession session, CartDto cart) {
        Assert.notNull(session, "Expected a non-null session");
        Assert.notNull(cart, "Expected a non-null cart");

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
                final List<CatalogueItemDetailsDto> result = this.catalogueService.findAllPublishedById(keys);

                final List<CatalogueItemDetailsDto> catalogueItems = result.stream()
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
                    cartItem.getQuotationParameters().setUserName(this.currentUserEmail());

                    final EffectivePricingModelDto pricingModel = quotationService.createQuotation(
                        catalogueItem, cartItem.getPricingModelKey(), cartItem.getQuotationParameters(), false
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
