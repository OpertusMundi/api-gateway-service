package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDraftDetailsDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDraftDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CatalogueEndpointTypes {

    @Schema(description = "Asset collection response")
    public static class ItemCollectionResponse extends CatalogueClientCollectionResponse<CatalogueItemDto> {

    }

    @Schema(description = "Draft collection response")
    public static class DraftCollectionResponse extends CatalogueClientCollectionResponse<CatalogueItemDraftDto> {

    }

    @Schema(description = "Asset response")
    public class ItemResponse extends RestResponse<CatalogueItemDetailsDto> {

    }

    @Schema(description = "Draft response")
    public class DraftItemResponse extends RestResponse<CatalogueItemDraftDetailsDto> {

    }

}
