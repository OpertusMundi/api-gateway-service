package eu.opertusmundi.web.model.catalogue.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogueClientSetStatusCommandDto {

    @Schema(description = "Item new status")
    private EnumDraftStatus status;

}
