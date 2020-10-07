package eu.opertusmundi.web.model.catalogue.server;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogueResponse<R> {

    private R result;

    private boolean success;

    private CatalogueMessage message;

}
