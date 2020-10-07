package eu.opertusmundi.web.model.order;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CartDto {

    @JsonIgnore
    @Getter
    @Setter
    private UUID key;

    @JsonIgnore
    @Getter
    @Setter
    private Integer accountId;

    private final List<CartItemDto> items = new ArrayList<CartItemDto>();

    @ArraySchema(
        arraySchema = @Schema(
            description = "Selected items"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = CartItemDto.class)
    )
    public CartItemDto[] getItems() {
        return this.items.stream().toArray(CartItemDto[]::new);
    }

    @ArraySchema(
        arraySchema = @Schema(
            description = "Applied discount coupons (for future use)"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    @Setter
    private String[] appliedCoupons;

    @JsonProperty("totalPrice")
    @Schema(description = "Total price including tax")
    public BigDecimal getTotalPrice() {
        return this.items.stream()
            .map(i -> i.getPricingModel() == null ? BigDecimal.ZERO : i.getPricingModel().getTotalPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @JsonProperty("totalPriceExcludingTax")
    @Schema(description = "Total price excluding tax")
    public BigDecimal getTotalPriceExcludingTax() {
        return this.items.stream()
            .map(i -> i.getPricingModel() == null ? BigDecimal.ZERO : i.getPricingModel().getTotalPriceExcludingTax())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @JsonProperty("taxTotal")
    @Schema(description = "Total tax")
    public BigDecimal getTaxTotal() {
        return this.items.stream()
            .map(i -> i.getPricingModel() == null ? BigDecimal.ZERO : i.getPricingModel().getTax())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Schema(description = "Currency of monetary values", implementation = String.class, example = "EUR")
    @Getter
    @Setter
    protected Currency currency;

    @Schema(description = "Number of items in the cart")
    @Getter
    @Setter
    private int totalItems;

    @Schema(description = "Cart creation date", example = "2020-06-10T16:01:04.991+03:00")
    @Getter
    @Setter
    private ZonedDateTime createdAt;

    @Schema(description = "Cart most recent update date", example = "2020-06-10T16:01:04.991+03:00")
    @Getter
    @Setter
    private ZonedDateTime modifiedAt;

    public void addItem(CartItemDto item) {
        this.items.add(item);
    }

}
