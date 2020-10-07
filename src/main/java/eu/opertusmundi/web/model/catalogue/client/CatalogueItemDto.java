package eu.opertusmundi.web.model.catalogue.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.web.model.openapi.schema.PricingModelAsJson;
import eu.opertusmundi.web.model.pricing.BasePricingModelDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CatalogueItemDto extends BaseCatalogueItemDto {

    public CatalogueItemDto(CatalogueFeature feature) {
        super(feature);

        this.id = UUID.fromString(feature.getId());

        this.publisherId = feature.getProperties().getPublisherId();
        this.statistics  = feature.getProperties().getStoreStatistics();

        // Initialize with an empty collection. Caller must compute the
        // effective pricing models
        this.pricingModels = new ArrayList<BasePricingModelDto>();
    }

    @Schema(description = "Catalogue item identifier (UUID)", example = "f5edff99-426b-4b17-a4f8-3d423a6c491b")
    @Getter
    @Setter
    private UUID id;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported pricing models"
        ),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = PricingModelAsJson.class)
    )
    @Getter
    @Setter
    private List<BasePricingModelDto> pricingModels;

    @Schema(description = "Id of an entity responsible for making the resource available")
    @Getter
    @Setter
    protected UUID publisherId;

    @Schema(description = "Asset statistics")
    @Getter
    @Setter
    private CatalogueItemStoreStatistics statistics;

}
