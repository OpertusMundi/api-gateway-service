package eu.opertusmundi.web.model.message.server;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerRecipientDto {

    private UUID id;

    private String name;

}
