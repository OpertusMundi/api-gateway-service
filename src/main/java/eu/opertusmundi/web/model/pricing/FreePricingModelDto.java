package eu.opertusmundi.web.model.pricing;

import java.math.BigDecimal;
import java.util.UUID;

public class FreePricingModelDto extends BasePricingModelDto {

    private static final long serialVersionUID = 1L;

    public FreePricingModelDto() {
        super(EnumPricingModel.FREE);
    }

    public FreePricingModelDto(UUID key, BigDecimal taxPercent) {
        this();

        this.key                    = key;
        this.taxPercent             = taxPercent.intValue();
        this.totalPriceExcludingTax = new BigDecimal(0);
        this.tax                    = new BigDecimal(0);
        this.totalPrice             = new BigDecimal(0);
    }

}