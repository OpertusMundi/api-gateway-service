package eu.opertusmundi.web.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class FixedPricingModelDto extends BasePricingModelDto {

    private static final long serialVersionUID = 1L;

    public FixedPricingModelDto() {
        super(EnumPricingModel.FIXED);
    }

    public FixedPricingModelDto(UUID key, BigDecimal totalPriceExcludingTax, BigDecimal taxPercent, boolean includesUpdates, int yearsOfUpdates) {
        this();

        this.key = key;
        this.taxPercent             = taxPercent.intValue();
        this.includesUpdates        = includesUpdates;
        this.yearsOfUpdates         = yearsOfUpdates;
        this.totalPriceExcludingTax = totalPriceExcludingTax;

        this.tax = this.totalPriceExcludingTax
            .multiply(taxPercent)
            .divide(new BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP);

        this.totalPrice = this.totalPriceExcludingTax.add(this.tax);
    }

    @Schema(description = "True if pricing model includes updates")
    @Getter
    @Setter
    private boolean includesUpdates;

    @Schema(description = "Number of years for included updates")
    @Getter
    @Setter
    private int yearsOfUpdates;

}