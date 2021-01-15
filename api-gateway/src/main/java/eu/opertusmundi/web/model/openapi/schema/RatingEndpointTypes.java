package eu.opertusmundi.web.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.rating.client.ClientRatingDto;

public class RatingEndpointTypes {

    public static class AssetResponse extends RestResponse<List<ClientRatingDto>> {

    }

    public static class ProviderResponse extends RestResponse<List<ClientRatingDto>> {

    }

}
