package eu.opertusmundi.web.model.catalogue.client;

import eu.opertusmundi.common.model.PageRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Catalogue query")
public class CatalogueSearchQuery extends PageRequestDto {

    @Schema(description = "Query string used for full text search operation")
    @Getter
    @Setter
    private String query;

    public PageRequestDto toPageRequest() {
        return new PageRequestDto(this.page, this.size);
    }

}
