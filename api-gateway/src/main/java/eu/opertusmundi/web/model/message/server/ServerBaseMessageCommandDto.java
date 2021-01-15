package eu.opertusmundi.web.model.message.server;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerBaseMessageCommandDto {

    protected EnumMessageType type;

    private UUID recipient;

    private String text;

}
