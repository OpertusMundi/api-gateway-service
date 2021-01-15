package eu.opertusmundi.web.model.message.client;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.web.model.message.server.EnumMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ClientBaseMessageDto {

    public ClientBaseMessageDto(EnumMessageType type) {
        this.type = type;
    }

    @JsonIgnore
    @Getter
    private final EnumMessageType type;

    @Schema(description = "Message unique id")
    @Getter
    @Setter
    private UUID id;

    @Schema(description = "Message text")
    @NotEmpty
    @Getter
    @Setter
    private String text;

    @Schema(description = "Created at")
    @Getter
    @Setter
    private ZonedDateTime createdAt;

    @Schema(description = "Read at")
    @Getter
    @Setter
    private ZonedDateTime readAt;

    @Schema(description = "Message is marked as read")
    @Getter
    @Setter
    private boolean read;

    @Schema(description = "Message recipient")
    @Getter
    @Setter
    private UUID recipient;

}
