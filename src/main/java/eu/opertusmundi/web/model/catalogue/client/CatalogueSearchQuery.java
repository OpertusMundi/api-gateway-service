package eu.opertusmundi.web.model.catalogue.client;

import eu.opertusmundi.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Catalogue query")
public class CatalogueSearchQuery extends PageRequest {

    @Schema(description = "Query string used for full text search operation")
    @Getter
    @Setter
    private String query;

    public PageRequest toPageRequest() {
        return new PageRequest(this.page, this.size);
    }

}
