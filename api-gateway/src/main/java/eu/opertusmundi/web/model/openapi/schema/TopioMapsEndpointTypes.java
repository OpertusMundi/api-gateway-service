package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.AccountMapDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class TopioMapsEndpointTypes {

    @Schema(description = "Topio map collection response")
    public static class MapsCollectionResponse extends RestResponse<PageResultDto<AccountMapDto>> {

    }

}
