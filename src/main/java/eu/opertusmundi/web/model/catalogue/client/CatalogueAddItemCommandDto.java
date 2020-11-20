package eu.opertusmundi.web.model.catalogue.client;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.web.model.openapi.schema.PricingModelCommandAsJson;
import eu.opertusmundi.web.model.pricing.BasePricingModelCommandDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueAddItemCommandDto extends BaseCatalogueItemDto {

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported pricing models"
        ),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = PricingModelCommandAsJson.class)
    )
    private List<BasePricingModelCommandDto> pricingModels;

    /**
     * Asset unique id. This value is injected by the controller.
     */
    @JsonIgnore
    private UUID id;

    /**
     * Publisher unique id.
     *
     * This value is ignored during serialization/deserialization. Instead, it
     * is injected by the controller. The value is equal to the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID publisherId;

    public CatalogueFeature toFeature() {
        return new CatalogueFeature(this);
    }

}
