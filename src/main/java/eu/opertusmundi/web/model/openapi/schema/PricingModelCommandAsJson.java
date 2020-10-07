package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.web.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.FreePricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelCommandDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    oneOf = {
        FreePricingModelCommandDto.class,
        FixedPricingModelCommandDto.class,
        SubscriptionPricingModelCommandDto.class
    }
)
public class PricingModelCommandAsJson {

}
