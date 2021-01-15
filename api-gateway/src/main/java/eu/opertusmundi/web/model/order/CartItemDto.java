package eu.opertusmundi.web.model.order;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.openapi.schema.PricingModelAsJson;
import eu.opertusmundi.common.model.pricing.BasePricingModelDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CartItemDto {

    @Schema(description = "Cart item unique identifier")
    @JsonProperty("id")
    @Getter
    @Setter
    private UUID key;

    @JsonIgnore
    @Getter
    @Setter
    private UUID productKey;

    @Schema(description = "Catalogue item")
    @JsonProperty("product")
    @Getter
    @Setter
    private CatalogueItemDto product;

    @Schema(description = "Date added to the cart")
    @Getter
    @Setter
    private ZonedDateTime addedAt;

    @JsonIgnore
    @Getter
    @Setter
    private UUID pricingModelKey;

    @Schema(implementation = PricingModelAsJson.class, description = "Selected pricing model")
    @JsonProperty("pricingModel")
    @Getter
    @Setter
    private BasePricingModelDto pricingModel;

    /*
    @Schema(description = "True if asset is available for online deliver")
    @Getter
    @Setter
    private boolean deliveredOnline;

    @Schema(description = "True if asset is available for offline deliver")
    @Getter
    @Setter
    private boolean deliveredOffline;
    */

}
