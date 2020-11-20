package eu.opertusmundi.web.model.catalogue.client;

import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CatalogueItemDraftDto extends CatalogueItemDto {

    public CatalogueItemDraftDto(CatalogueFeature feature) {
        super(feature);

        this.status = EnumDraftStatus.fromValue(feature.getProperties().getStatus());
    }

    @Getter
    @Setter
    private EnumDraftStatus status;

}
