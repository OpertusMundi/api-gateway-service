package eu.opertusmundi.web.feign.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.web.feign.client.config.CatalogueFeignClientConfiguration;
import eu.opertusmundi.web.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.web.model.catalogue.server.CatalogueResponse;

@FeignClient(
    name = "${opertusmundi.feign.catalogue.name}",
    url = "${opertusmundi.feign.catalogue.url}",
    configuration = CatalogueFeignClientConfiguration.class
)
public interface CatalogueFeignClient {

    /**
     * Search catalogue items
     *
     * @param query Search query
     * @param pageIndex The page index. Page index is 1-based
     * @param pageSize The page size
     * @return An instance of {@link CatalogCollection}
     */
    @GetMapping(value = "/api/catalogue/search")
    ResponseEntity<CatalogueResponse<CatalogueCollection>> find(
        @RequestParam("q") String query,
        @RequestParam("page") int pageIndex,
        @RequestParam("per_page") int pageSize
    );

    /**
     * Get an item by id
     *
     * @param id The item unique identifier.
     * @return An instance of {@link CatalogueFeature}
     */
    @GetMapping(value = "/api/catalogue/{id}", produces = "application/json")
    ResponseEntity<CatalogueResponse<CatalogueFeature>> findOneById(@PathVariable("id") UUID id);

    /**
     * Get a set of items item by their identifiers
     *
     * @param id The item unique identifier.
     * @return An instance of {@link CatalogueServerItem}
     */
    @GetMapping(value = "/api/catalogue/get", produces = "application/json")
    ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> findAllById(@RequestParam("id") UUID[] id);

    // TODO: Items must be created in DRAFT state

    /**
     * Create a new item
     *
     * @param feature The feature to create
     */
    @PostMapping(value = "/api/catalogue/", produces = "application/json")
    ResponseEntity<Void> create(@RequestBody CatalogueFeature feature);
}
