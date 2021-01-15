package eu.opertusmundi.web.model.message.server;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerMessageCommandDto extends ServerBaseMessageCommandDto {

    public ServerMessageCommandDto() {
        this.type = EnumMessageType.MESSAGE;
    }

    private UUID recipient;

    private String text;

    private UUID sender;

    private UUID message;

}
