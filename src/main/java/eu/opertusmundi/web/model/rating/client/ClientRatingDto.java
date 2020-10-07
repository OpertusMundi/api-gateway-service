package eu.opertusmundi.web.model.rating.client;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import eu.opertusmundi.web.model.rating.server.ServerRatingDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Rating object")
@NoArgsConstructor
public class ClientRatingDto {

    public ClientRatingDto(ServerRatingDto r) {
        this.comment   = r.getComment();
        this.createdAt = r.getCreatedAt();
        this.value     = r.getValue();
    }

    @Schema(description = "Rating value", minimum = "0", maximum = "5")
    @Getter
    @Setter
    protected BigDecimal value;

    @Schema(description = "User comment")
    @Getter
    @Setter
    protected String comment;

    @Schema(description = "Rating date")
    @Getter
    @Setter
    protected ZonedDateTime createdAt;

}
