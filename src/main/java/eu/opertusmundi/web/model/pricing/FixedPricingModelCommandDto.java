package eu.opertusmundi.web.model.pricing;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class FixedPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public FixedPricingModelCommandDto() {
        super(EnumPricingModel.FIXED);
    }

    @Schema(description = "True if pricing model includes updates")
    @Getter
    @Setter
    private boolean includesUpdates;

    @Schema(description = "Number of years for included updates")
    @Getter
    @Setter
    private int yearsOfUpdates;

    @Schema(description = "Price excluding tax")
    @Getter
    @Setter
    protected BigDecimal totalPriceExcludingTax;

}