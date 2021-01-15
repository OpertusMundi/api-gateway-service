package eu.opertusmundi.web.model.message.client;

import java.util.UUID;

import eu.opertusmundi.web.model.message.server.EnumMessageType;
import eu.opertusmundi.web.model.message.server.ServerNotificationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ClientNotificationDto extends ClientBaseMessageDto {

    public ClientNotificationDto() {
        super(EnumMessageType.NOTIFICATION);
    }

    public ClientNotificationDto(ServerNotificationDto n) {
        this();

        this.setCreatedAt(n.getCreatedAt());
        this.setId(n.getId());
        this.setRead(n.isRead());
        this.setReadAt(n.getReadAt());
        this.setRecipient(n.getRecipient());
        this.setText(n.getText());
    }

    @Schema(description = "Notification recipient")
    @Getter
    @Setter
    private UUID recipient;

}
