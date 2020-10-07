package eu.opertusmundi.web.model.rating.server;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

public class ServerRatingDto {

    @Getter
    @Setter
    protected BigDecimal value;

    @Getter
    @Setter
    protected String comment;

    @Getter
    @Setter
    protected ZonedDateTime createdAt;

}
