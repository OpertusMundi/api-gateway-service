package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.QueryResultPage;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.message.client.ClientNotificationDto;

public class MessageEndpointTypes {

    public static class MessageReceiptDto {

    }

    public static class NotificationListResponseDto extends RestResponse<QueryResultPage<ClientNotificationDto>> {

    }

}
