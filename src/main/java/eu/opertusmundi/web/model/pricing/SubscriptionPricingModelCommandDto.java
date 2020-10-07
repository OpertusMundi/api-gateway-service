package eu.opertusmundi.web.model.pricing;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class SubscriptionPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public SubscriptionPricingModelCommandDto() {
        super(EnumPricingModel.SUBSCRIPTION);
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