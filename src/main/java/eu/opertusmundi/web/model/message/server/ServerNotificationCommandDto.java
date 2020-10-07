package eu.opertusmundi.web.model.message.server;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerNotificationCommandDto extends ServerBaseMessageCommandDto {

    public ServerNotificationCommandDto() {
        this.type = EnumMessageType.NOTIFICATION;
    }

}
