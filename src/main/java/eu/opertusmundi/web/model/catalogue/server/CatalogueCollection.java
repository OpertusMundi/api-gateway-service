package eu.opertusmundi.web.model.catalogue.server;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CatalogueCollection {

    private List<CatalogueFeature> items;

    private int page;

    private int pages;

    private int per_page;

    private int total;

}
