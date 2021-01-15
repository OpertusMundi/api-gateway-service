package eu.opertusmundi.web.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.web.model.order.CartDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Cart")
@Table(schema = "`order`", name = "`cart`")
public class CartEntity {

    @Id
    @SequenceGenerator(sequenceName = "`order.cart_id_seq`", name = "cart_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "cart_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Getter
    private Integer id;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`account`")
    @Getter
    @Setter
    private AccountEntity account;

    @Column(name = "`account`", insertable = false, updatable = false)
    private Integer accountId;

    @OneToMany(
        mappedBy = "cart", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true
    )
    @Getter
    @Setter
    private List<CartItemEntity> items = new ArrayList<>();

    @Column(name = "`total_price`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal totalPrice;

    @Column(name = "`total_price_excluding_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal totalPriceExcludingTax;

    @Column(name = "`total_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal taxTotal;

    @Column(name = "`currency`")
    @Getter
    @Setter
    private Currency currency;

    @Column(name = "`created_on`")
    @Getter
    private final ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "`modified_on`")
    @Getter
    @Setter
    private ZonedDateTime modifiedAt = ZonedDateTime.now();

    public CartEntity() {

    }

    public CartDto toDto() {
        final CartDto c = new CartDto();

        this.updateDto(c);

        return c;
    }

    private void updateDto(CartDto c) {
        if (this.account != null) {
            c.setAccountId(this.accountId);
        }

        c.setCreatedAt(this.createdAt);
        c.setCurrency(this.currency);
        c.setKey(this.key);
        c.setModifiedAt(this.modifiedAt);

        this.items.stream()
            .filter(i -> i.getRemovedAt() == null)
            .forEach(i -> c.addItem(i.toDto()));

        c.setTotalItems(c.getItems().length);
    }

}