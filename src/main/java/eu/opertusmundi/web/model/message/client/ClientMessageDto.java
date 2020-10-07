package eu.opertusmundi.web.model.message.client;

import java.util.UUID;

import eu.opertusmundi.web.model.message.server.EnumMessageType;
import eu.opertusmundi.web.model.message.server.ServerMessageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Message object")
public class ClientMessageDto extends ClientBaseMessageDto {

    public ClientMessageDto() {
        super(EnumMessageType.MESSAGE);
    }

    public ClientMessageDto(ServerMessageDto m) {
        this();

        this.setCreatedAt(m.getCreatedAt());
        this.setId(m.getId());
        this.setRead(m.isRead());
        this.setReadAt(m.getReadAt());
        this.setRecipient(m.getRecipient());
        this.setText(m.getText());
        this.setThread(m.getThread());
    }

    @Schema(description = "Message thread unique id")
    @Getter
    @Setter
    private UUID thread;

}
