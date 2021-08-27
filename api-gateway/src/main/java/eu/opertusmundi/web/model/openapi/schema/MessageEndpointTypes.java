package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.message.client.ClientMessageDto;
import eu.opertusmundi.common.model.message.client.ClientNotificationDto;

public class MessageEndpointTypes {

    public static class MessageResponseDto extends RestResponse<ClientMessageDto> {

    }

    public static class NotificationListResponseDto extends RestResponse<PageResultDto<ClientNotificationDto>> {

    }

}
