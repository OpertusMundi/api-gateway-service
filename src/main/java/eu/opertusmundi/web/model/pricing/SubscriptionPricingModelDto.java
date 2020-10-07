package eu.opertusmundi.web.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class SubscriptionPricingModelDto extends BasePricingModelDto {

    private static final long serialVersionUID = 1L;

    public SubscriptionPricingModelDto() {
        super(EnumPricingModel.SUBSCRIPTION);
    }

    public SubscriptionPricingModelDto(UUID key, int duration, BigDecimal taxPercent, BigDecimal monthlyPrice) {
        this();

        this.key = key;
        this.taxPercent   = taxPercent.intValue();
        this.duration     = duration;
        this.monthlyPrice = monthlyPrice;

        this.totalPriceExcludingTax = monthlyPrice.multiply(new BigDecimal(duration));

        this.tax = this.totalPriceExcludingTax
            .multiply(taxPercent)
            .divide(new BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP);

        this.totalPrice = this.totalPriceExcludingTax.add(this.tax);
    }

    @Schema(description = "Subscription duration in months")
    @Getter
    @Setter
    private int duration;

    @Schema(description = "Monthly subscription price excluding tax")
    @Getter
    @Setter
    private BigDecimal monthlyPrice;

}