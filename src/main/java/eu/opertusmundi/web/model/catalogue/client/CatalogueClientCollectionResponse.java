package eu.opertusmundi.web.model.catalogue.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.opertusmundi.common.model.QueryResultPage;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.PublisherDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Catalogue asset response")
@NoArgsConstructor
public class CatalogueClientCollectionResponse extends RestResponse<QueryResultPage<CatalogueItemDto>> {

    public CatalogueClientCollectionResponse(QueryResultPage<CatalogueItemDto> result, List<PublisherDto> publishers) {
        super(result);

        this.publishers = new HashMap<UUID, PublisherDto>();

        publishers.stream().forEach(p -> {
            if (!this.publishers.containsKey(p.getKey())) {
                this.publishers.put(p.getKey(), p);
            }
        });
    }

    @Schema(description = "Map with all publishers for all catalogue items in the response. The key is the publisher id.")
    @Getter
    @Setter
    private Map<UUID, PublisherDto> publishers;

}
