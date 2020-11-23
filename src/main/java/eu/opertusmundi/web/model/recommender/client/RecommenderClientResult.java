package eu.opertusmundi.web.model.recommender.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.dto.PublisherDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class RecommenderClientResult {

    public RecommenderClientResult() {
        this.assets     = new ArrayList<CatalogueItemDto>();
        this.publishers = new HashMap<UUID, PublisherDto>();
    }

    public RecommenderClientResult(List<CatalogueItemDto> assets, List<PublisherDto> publishers) {
        this.assets = assets;

        this.publishers = new HashMap<UUID, PublisherDto>();

        publishers.stream().forEach(p -> {
            if (!this.publishers.containsKey(p.getKey())) {
                this.publishers.put(p.getKey(), p);
            }
        });
    }

    @Schema(description = "Recommended assets")
    @Getter
    private final List<CatalogueItemDto> assets;

    @Schema(description = "Map with all publishers for all catalogue assets in the response. The key is the publisher id.")
    @Getter
    private final Map<UUID, PublisherDto> publishers;

}
