package eu.opertusmundi.web.model.recommender.client;

import java.util.List;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recommender response")
public class RecommenderClientResponse extends RestResponse<RecommenderClientResult> {

    public RecommenderClientResponse() {
        super(new RecommenderClientResult());
    }

    public RecommenderClientResponse(RecommenderClientResult result) {
        super(result);
    }

    public RecommenderClientResponse(List<CatalogueItemDto> assets, List<PublisherDto> publishers) {
        super(new RecommenderClientResult(assets, publishers));
    }

}
