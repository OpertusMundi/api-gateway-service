package eu.opertusmundi.web.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.web.model.order.CartAddCommandDto;
import eu.opertusmundi.web.model.order.CartDto;

@Service
public class DefaultCartService implements CartService {

    @Autowired
    private CartStore cartStore;

    @Override
    public CartDto getCart(final UUID cartKey) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        return this.cartStore.getCart(effectiveCartKey);
    }

    @Override
    public CartDto addItem(final UUID cartKey, CartAddCommandDto command) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        return this.cartStore.addItem(effectiveCartKey, command);
    }

    @Override
    public CartDto removeItem(final UUID cartKey, UUID itemKey) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        try {
            return this.cartStore.removeItem(effectiveCartKey, itemKey);
        } catch (final Exception ex) {
            // Ignore exception
        }

        // TODO: Add error message
        return this.cartStore.getCart(effectiveCartKey);
    }

    @Override
    public CartDto clear(final UUID cartKey) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        return this.cartStore.clear(effectiveCartKey);
    }

    @Override
    public void setAccount(final UUID cartKey, Integer accountId) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        this.cartStore.setAccount(effectiveCartKey, accountId);
    }

    private UUID ensureCart(UUID cartKey) {
        if (cartKey == null) {
            final CartDto cart = this.cartStore.create();

            return cart.getKey();
        }

        return cartKey;
    }

}
