package eu.opertusmundi.web.model.rating.server;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

public class ServerBaseRatingCommandDto {

    @NotNull
    @Getter
    @Setter
    private UUID account;

    @NotNull
    @Getter
    @Setter
    protected BigDecimal value;

    @Getter
    @Setter
    protected String comment;

}
