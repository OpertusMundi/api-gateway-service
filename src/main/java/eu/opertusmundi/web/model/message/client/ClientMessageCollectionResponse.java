package eu.opertusmundi.web.model.message.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.opertusmundi.common.model.QueryResultPage;
import eu.opertusmundi.common.model.RestResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Message collection response")
@NoArgsConstructor
public class ClientMessageCollectionResponse extends RestResponse<QueryResultPage<ClientMessageDto>> {

    public ClientMessageCollectionResponse(QueryResultPage<ClientMessageDto> result, List<ClientRecipientDto> recipients) {
        super(result);

        this.recipients = new HashMap<UUID, ClientRecipientDto>();

        recipients.stream().forEach(r -> {
            if (!this.recipients.containsKey(r.getId())) {
                this.recipients.put(r.getId(), r);
            }
        });
    }

    @Schema(description = "Map with all recipients for all messages in the response. The key is the recipient key.")
    @Getter
    @Setter
    private Map<UUID, ClientRecipientDto> recipients;

}
