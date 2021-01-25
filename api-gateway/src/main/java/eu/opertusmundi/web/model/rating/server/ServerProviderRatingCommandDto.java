package eu.opertusmundi.web.model.rating.server;

import eu.opertusmundi.web.model.rating.client.ClientRatingCommandDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerProviderRatingCommandDto extends ServerBaseRatingCommandDto {

    public ServerProviderRatingCommandDto(ClientRatingCommandDto c) {
        this.comment = c.getComment();
        this.value   = c.getValue();
    }

}
