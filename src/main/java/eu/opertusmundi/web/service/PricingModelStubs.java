package eu.opertusmundi.web.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.opertusmundi.web.model.pricing.BasePricingModelDto;
import eu.opertusmundi.web.model.pricing.FixedPricingModelDto;
import eu.opertusmundi.web.model.pricing.FreePricingModelDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelDto;
import lombok.Getter;

@Component
public class PricingModelStubs {

    private final BigDecimal tax = new BigDecimal(24);

    public PricingModelStubs() {
        this.freeModel         = new FreePricingModelDto(
            UUID.fromString("33de4ef3-0b66-4530-9f7c-6dd78db5c80e"),
            this.tax
        );
        this.fixedModel1       = new FixedPricingModelDto(
            UUID.fromString("e88757b5-3b13-44a2-bc19-071737f40681"), new BigDecimal(200), this.tax, false, 0
        );
        this.fixedModel2       = new FixedPricingModelDto(
            UUID.fromString("3412bbe0-b0cd-4a33-8841-b6fc1bd22f10"), new BigDecimal(350), this.tax, true, 1
        );
        this.fixedModel3       = new FixedPricingModelDto(
            UUID.fromString("a404d872-c2b5-422a-a87c-fc4418f94d03"), new BigDecimal(480), this.tax, true, 2
        );
        this.subscriptionModel = new SubscriptionPricingModelDto(
            UUID.fromString("334669dc-b36c-41d7-8283-1870dcdf4b5f"), 12, new BigDecimal(24), new BigDecimal("10.99")
        );
    }

    @Getter
    private final BasePricingModelDto freeModel;

    @Getter
    private final BasePricingModelDto fixedModel1;

    @Getter
    private final BasePricingModelDto fixedModel2;

    @Getter
    private final BasePricingModelDto fixedModel3;

    @Getter
    private final BasePricingModelDto subscriptionModel;

}
