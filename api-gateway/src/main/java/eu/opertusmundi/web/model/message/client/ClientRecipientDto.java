package eu.opertusmundi.web.model.message.client;

import java.util.UUID;

import eu.opertusmundi.common.domain.AccountEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ClientRecipientDto {

    public ClientRecipientDto(AccountEntity e) {
        this.id   = e.getKey();
        this.name = e.getFullName();
        this.logoImage = e.getProfile().getProvider().getLogoImage();
        this.logoImageMimeType = e.getProfile().getProvider().getLogoImageMimeType();
    }

    @Schema(description = "User unique id")
    private UUID id;

    @Schema(description = "User full name")
    private String name;

    @Schema(description = "Company image")
    private byte[] logoImage;

    @Schema(description = "Company image mime type", example = "image/png")
    private String logoImageMimeType;

}
