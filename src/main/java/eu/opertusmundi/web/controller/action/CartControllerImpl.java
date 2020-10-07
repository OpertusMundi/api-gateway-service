package eu.opertusmundi.web.controller.action;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.feign.client.CatalogueFeignClient;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.web.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.web.model.order.CartAddCommandDto;
import eu.opertusmundi.web.model.order.CartDto;
import eu.opertusmundi.web.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.BasePricingModelDto;
import eu.opertusmundi.web.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.FixedPricingModelDto;
import eu.opertusmundi.web.model.pricing.FreePricingModelDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelDto;
import eu.opertusmundi.web.service.CartService;
import feign.FeignException;

@RestController
public class CartControllerImpl extends BaseController implements CartController {

    private final static String CART_SESSION_KEY = "Cart.Session.Id";

    // TODO: Set from configuration
    private final BigDecimal tax = new BigDecimal(24);

    @Autowired
    private CartService cartService;

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Override
    public RestResponse<CartDto> getCart(HttpSession session) {
        final UUID cartId = (UUID) session.getAttribute(CART_SESSION_KEY);

        final CartDto cart = this.cartService.getCart(cartId);

        this.updateCart(session, cart);

        return RestResponse.result(cart);
    }

    @Override
    public RestResponse<CartDto> addItem(CartAddCommandDto command, HttpSession session) {
        final UUID cartId = (UUID) session.getAttribute(CART_SESSION_KEY);

        final CartDto cart = this.cartService.addItem(cartId, command);

        this.updateCart(session, cart);

        return RestResponse.result(cart);
    }

    @Override
    public RestResponse<CartDto> removeItem(UUID itemId, HttpSession session) {
        final UUID cartId = (UUID) session.getAttribute(CART_SESSION_KEY);

        final CartDto cart = this.cartService.removeItem(cartId, itemId);

        this.updateCart(session, cart);

        return RestResponse.result(cart);
    }

    @Override
    public RestResponse<CartDto> clear(HttpSession session) {
        final UUID cartId = (UUID) session.getAttribute(CART_SESSION_KEY);

        final CartDto cart = this.cartService.clear(cartId);

        this.updateCart(session, cart);

        return RestResponse.result(cart);
    }

    private void updateCart(HttpSession session, CartDto cart) {
        // Update cart key in session
        session.setAttribute(CART_SESSION_KEY, cart.getKey());
        // Link authenticated user to the cart
        if (cart.getAccountId() == null && this.currentUserId() != null) {
            this.cartService.setAccount(cart.getKey(), this.currentUserId());
        }
        // Inject catalogue data
        final UUID[] keys = Arrays.stream(cart.getItems()).map(i -> i.getProductKey()).toArray(UUID[]::new);

        if(keys.length != 0) {
            ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> e;

            try {
                e = this.catalogueClient.getObject().findAllById(keys);
            } catch (final FeignException fex) {
                // TODO: Handle error
                return;
            }

            final CatalogueResponse<List<CatalogueFeature>> catalogueResponse = e.getBody();

            if(catalogueResponse.isSuccess()) {
                final List<CatalogueItemDto> items = catalogueResponse.getResult().stream()
                    .map(feature -> {
                        final CatalogueItemDto item = new CatalogueItemDto(feature);

                        // Compute effective pricing models
                        this.refreshPricingModels(item, feature.getProperties().getPricingModels());

                        return item;
                    }).collect(Collectors.toList());

                // Set product and selected pricing model
                Arrays.stream(cart.getItems()).forEach(cartItem -> {
                    final CatalogueItemDto catalogueItem = items.stream()
                        .filter(i -> i.getId().equals(cartItem.getProductKey()))
                        .findFirst()
                        .orElse(null);

                    // TODO : Add additional error handling (cart items must exist in the catalogue)
                    if (catalogueItem != null) {
                        cartItem.setProduct(catalogueItem);
                    }

                    // TODO: Add additional error handling for pricing model (selected pricing model must exist in the catalog)
                    if (catalogueItem != null) {
                        final BasePricingModelDto pm = cartItem.getProduct().getPricingModels()
                            .stream()
                            .filter(p -> p.getKey().equals(cartItem.getPricingModelKey()))
                            .findFirst().orElse(null);

                        cartItem.setPricingModel(pm);
                    }
                });
            } else {
                // TODO: Handle error
            }
        }
    }

    // TODO : Move to new service

    private void refreshPricingModels(CatalogueItemDto item, List<BasePricingModelCommandDto> models) {
        models.forEach(m -> {
            final UUID key = m.getKey();

            switch (m.getType()) {
                case FREE :
                    item.getPricingModels().add(new FreePricingModelDto(
                        key,
                        this.tax
                    ));
                    break;
                case FIXED :
                    final FixedPricingModelCommandDto fixed = (FixedPricingModelCommandDto) m;

                    item.getPricingModels().add(new FixedPricingModelDto(
                        key,
                        fixed.getTotalPriceExcludingTax(),
                        this.tax,
                        fixed.isIncludesUpdates(),
                        fixed.getYearsOfUpdates()
                    ));
                    break;
                case SUBSCRIPTION :
                    final SubscriptionPricingModelCommandDto subscription = (SubscriptionPricingModelCommandDto) m;

                    item.getPricingModels().add(new SubscriptionPricingModelDto(
                        key,
                        subscription.getDuration(),
                        this.tax,
                        subscription.getMonthlyPrice()
                    ));
                    break;
                default :
                    // Do nothing
                    break;
            }
        });
    }

}