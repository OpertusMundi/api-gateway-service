package eu.opertusmundi.web.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.domain.CartEntity;
import eu.opertusmundi.web.domain.CartItemEntity;
import eu.opertusmundi.web.model.order.CartAddCommandDto;
import eu.opertusmundi.web.model.order.CartDto;

@Transactional(readOnly = true)
@Repository
public interface CartRepository extends JpaRepository<CartEntity, Integer> {

    @Query("From Account a where a.id = :id")
    Optional<AccountEntity> getAccountById(Integer id);

    Optional<CartEntity> findOneByKey(UUID key);

    @Transactional(readOnly = false)
    default CartDto create(Integer accountId) {
        final CartEntity cart = new CartEntity();

        if (accountId != null) {
            final AccountEntity account = this.getAccountById(accountId).orElse(null);

            if (account == null) {
                throw new EntityNotFoundException();
            }

            cart.setAccount(account);
        }

        cart.setTaxTotal(new BigDecimal(0.00));
        cart.setTotalPrice(new BigDecimal(0.00));
        cart.setTotalPriceExcludingTax(new BigDecimal(0.00));
        cart.setCurrency(Currency.getInstance("EUR"));

        return this.saveAndFlush(cart).toDto();
    }

    @Transactional(readOnly = false)
    default void setAccount(UUID cartKey, Integer accountId) {
        Assert.notNull(cartKey, "Cart key must not be null");
        Assert.notNull(accountId, "Account id must not be null");

        final CartEntity cart = this.findOneByKey(cartKey).orElse(null);

        if (cart == null) {
            throw new EntityNotFoundException();
        }

        if (cart.getAccount() != null) {
            throw new EntityExistsException();
        }

        final AccountEntity account = this.getAccountById(accountId).orElse(null);

        if (account == null) {
            throw new EntityNotFoundException();
        }

        cart.setAccount(account);

        this.saveAndFlush(cart);
    }

    @Transactional(readOnly = false)
    default CartDto addItem(UUID cartKey, CartAddCommandDto command) {
        Assert.notNull(cartKey, "Cart key must not be null");
        Assert.notNull(command, "Command must not be null");

        final CartEntity cart = this.findOneByKey(cartKey).orElse(null);

        if (cart == null) {
            throw new EntityNotFoundException();
        }

        CartItemEntity item = cart.getItems().stream()
            .filter(i -> i.getProduct().equals(command.getProductId()) && i.getRemovedAt() == null)
            .findFirst().orElse(null);

        if (item != null) {
            // Update pricing model
            item.setPricingModel(command.getPricingModelId());
        } else {
            // Add item
            item = new CartItemEntity();

            item.setCart(cart);
            item.setProduct(command.getProductId());
            item.setPricingModel(command.getPricingModelId());

            cart.getItems().add(item);
        }
        // Always update modified time
        cart.setModifiedAt(item.getAddedAt());

        return this.saveAndFlush(cart).toDto();
    }


    @Transactional(readOnly = false)
    default CartDto removeItem(UUID cartKey, UUID itemKey) {
        Assert.notNull(cartKey, "Cart key must not be null");
        Assert.notNull(itemKey, "Item must not be null");

        final CartEntity cart = this.findOneByKey(cartKey).orElse(null);

        if (cart == null) {
            throw new EntityNotFoundException("cart");
        }

        final CartItemEntity item = cart.getItems().stream()
            .filter(i -> i.getKey().equals(itemKey))
            .findFirst()
            .orElse(null);

        if (item == null) {
            throw new EntityNotFoundException("item");
        }

        if (item.getRemovedAt() == null) {
            item.setRemovedAt(ZonedDateTime.now());

            cart.setModifiedAt(item.getRemovedAt());
        }

        return this.saveAndFlush(cart).toDto();
    }

    @Transactional(readOnly = false)
    default CartDto clear(UUID cartKey) {
        Assert.notNull(cartKey, "Cart key must not be null");

        final CartEntity cart = this.findOneByKey(cartKey).orElse(null);

        if (cart == null) {
            throw new EntityNotFoundException();
        }

        final ZonedDateTime removedAt = ZonedDateTime.now();

        cart.getItems().stream().forEach(i -> i.setRemovedAt(removedAt));
        cart.setModifiedAt(removedAt);

        return this.saveAndFlush(cart).toDto();
    }

}
