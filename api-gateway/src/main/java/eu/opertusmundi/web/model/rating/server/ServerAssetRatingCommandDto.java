package eu.opertusmundi.web.model.rating.server;

import eu.opertusmundi.web.model.rating.client.ClientRatingCommandDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerAssetRatingCommandDto extends ServerBaseRatingCommandDto {

    public ServerAssetRatingCommandDto(ClientRatingCommandDto c) {
        this.comment = c.getComment();
        this.value   = c.getValue();
    }

}
