package eu.opertusmundi.web.service;

import java.util.UUID;

import eu.opertusmundi.web.model.order.CartAddCommandDto;
import eu.opertusmundi.web.model.order.CartDto;

public interface CartStore {

    default CartDto create() {
        return this.create(null);
    }

    CartDto create(Integer accountId);

    CartDto getCart(UUID cartKey);

    CartDto addItem(UUID cartKey, CartAddCommandDto command);

    CartDto removeItem(UUID cartKey, UUID itemKey);

    CartDto clear(UUID cartKey);

    void setAccount(UUID cartKey, Integer accountId);

}
