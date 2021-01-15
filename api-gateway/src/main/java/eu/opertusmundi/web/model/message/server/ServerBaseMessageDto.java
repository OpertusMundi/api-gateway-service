package eu.opertusmundi.web.model.message.server;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerBaseMessageDto {

    private UUID id;

    private String text;

    private ZonedDateTime createdAt;

    private ZonedDateTime readAt;

    private boolean read;

    private UUID recipient;

}
