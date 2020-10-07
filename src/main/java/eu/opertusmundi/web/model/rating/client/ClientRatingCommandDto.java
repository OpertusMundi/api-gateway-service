package eu.opertusmundi.web.model.rating.client;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Rating command")
public class ClientRatingCommandDto {

    @Schema(description = "Rating value", minimum = "0", maximum = "5")
    @NotNull
    @Getter
    @Setter
    protected BigDecimal value;

    @Schema(description = "User comment")
    @Getter
    @Setter
    protected String comment;

}
