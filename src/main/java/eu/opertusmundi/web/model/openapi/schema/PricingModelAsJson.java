package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.web.model.pricing.FixedPricingModelDto;
import eu.opertusmundi.web.model.pricing.FreePricingModelDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    oneOf = {
        FreePricingModelDto.class,
        FixedPricingModelDto.class,
        SubscriptionPricingModelDto.class
    }
)
public class PricingModelAsJson {

}
