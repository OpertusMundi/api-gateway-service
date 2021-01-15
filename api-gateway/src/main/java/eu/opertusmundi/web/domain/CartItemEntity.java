package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.web.model.order.CartItemDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CartItem")
@Table(name = "`cart_item`", schema = "order")
public class CartItemEntity {

    @Id
    @SequenceGenerator(sequenceName = "order.cart_item_id_seq", name = "cart_item_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "cart_item_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Getter
    @Setter
    private Integer id;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "`cart`")
    @Getter
    @Setter
    private CartEntity cart;


    @NotNull
    @Column(name = "product", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    private UUID product;

    @NotNull
    @Column(name = "pricing_model", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    private UUID pricingModel;

    @NotNull
    @Column(name = "`added_on`")
    @Getter
    ZonedDateTime addedAt = ZonedDateTime.now();

    @Column(name = "`removed_on`")
    @Getter
    @Setter
    ZonedDateTime removedAt;

    public CartItemEntity() {

    }

    public CartItemDto toDto() {
        final CartItemDto i = new CartItemDto();

        this.updateDto(i);

        return i;
    }

    private void updateDto(CartItemDto i) {
        i.setAddedAt(this.addedAt);
        i.setKey(this.key);
        i.setPricingModelKey(this.pricingModel);
        i.setProductKey(this.product);
    }

}