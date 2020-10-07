package eu.opertusmundi.web.model.catalogue.client;

import eu.opertusmundi.common.model.RestResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Catalogue query response")
public class CatalogueClientItemResponse extends RestResponse<CatalogueItemDetailsDto> {

}
