package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountTicketDto;

public class TicketEndpointTypes {

    public static class TicketListResponseDto extends RestResponse<PageResultDto<AccountTicketDto>> {

    }

}
