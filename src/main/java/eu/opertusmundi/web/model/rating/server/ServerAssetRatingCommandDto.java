package eu.opertusmundi.web.model.rating.server;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import eu.opertusmundi.web.model.rating.client.ClientRatingCommandDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ServerAssetRatingCommandDto extends ServerBaseRatingCommandDto {

    public ServerAssetRatingCommandDto(ClientRatingCommandDto c) {
        this.comment = c.getComment();
        this.value   = c.getValue();
    }

    @NotNull
    @Getter
    @Setter
    private UUID asset;

}
