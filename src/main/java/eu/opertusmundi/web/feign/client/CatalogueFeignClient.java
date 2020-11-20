package eu.opertusmundi.web.feign.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    @GetMapping(value = "/api/published/search")
    ResponseEntity<CatalogueResponse<CatalogueCollection>> findAll(
        @RequestParam("q") String query,
        @RequestParam(name = "publisher_id", required = false) String publisher,
        @RequestParam("page") int pageIndex,
        @RequestParam("per_page") int pageSize
    );

    /**
     * Get a set of items item by their identifiers
     *
     * @param id The item unique identifier.
     * @return An instance of {@link CatalogueServerItem}
     */
    @GetMapping(value = "/api/get", produces = "application/json")
    ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> findAllById(@RequestParam("id") UUID[] id);

    /**
     * Get an item by id
     *
     * @param id The item unique identifier.
     * @return An instance of {@link CatalogueFeature}
     */
    @GetMapping(value = "/api/published/{id}", produces = "application/json")
    ResponseEntity<CatalogueResponse<CatalogueFeature>> findOneById(@PathVariable("id") UUID id);

    /**
     * Delete a published asset
     *
     * @param id The identifier of the asset to update
     * @return
     */
    @DeleteMapping(value = "/api/published/{id}", produces = "application/json")
    ResponseEntity<Void> deletePublished(@PathVariable("id") UUID id);

    /**
     * Search draft items
     *
     * @param publisher (Optional) Publisher unique id
     * @param status (Optional) Draft item status
     * @param pageIndex The page index. Page index is 1-based
     * @param pageSize The page size
     * @return An instance of {@link CatalogCollection}
     */
    @GetMapping(value = "/api/draft/search")
    ResponseEntity<CatalogueResponse<CatalogueCollection>> findAllDraft(
        @RequestParam(name = "publisher_id", required = false) UUID publisher,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "page", defaultValue = "1") int pageIndex,
        @RequestParam(name = "per_page", defaultValue = "10") int pageSize
    );

    /**
     * Get a draft item by id
     *
     * @param id The item unique identifier.
     * @return An instance of {@link CatalogueFeature}
     */
    @GetMapping(value = "/api/draft/{id}", produces = "application/json")
    ResponseEntity<CatalogueResponse<CatalogueFeature>> findOneDraftById(@PathVariable("id") UUID id);

    /**
     * Create a new draft item
     *
     * @param feature The feature to create
     */
    @PostMapping(value = "/api/draft/create", produces = "application/json")
    ResponseEntity<Void> createDraft(@RequestBody CatalogueFeature feature);

    /**
     * Create a new draft item from an existing catalogue item
     *
     * @param feature The feature to create
     */
    @PostMapping(value = "/api/draft/{id}", produces = "application/json")
    ResponseEntity<Void> createDraftFromPublished(@PathVariable("id") UUID id);

    /**
     * Create a new draft item
     *
     * @param feature The feature to create
     */
    @PutMapping(value = "/api/draft/{id}", produces = "application/json")
    ResponseEntity<Void> updateDraft(@PathVariable("id") UUID id, @RequestBody CatalogueFeature feature);

    /**
     * Update draft item status
     *
     * @param id The identifier of the draft to update
     * @param status The new status value
     * @return
     */
    @PutMapping(value = "/api/draft/status", produces = "application/json")
    ResponseEntity<Void> setDraftStatus(
        @RequestParam(name = "id", required = true) UUID id,
        @RequestParam(name = "status", required = true) String status
    );

    /**
     * Delete a draft item
     * @param id The identifier of the draft to update
     * @return
     */
    @DeleteMapping(value = "/api/draft/{id}", produces = "application/json")
    ResponseEntity<Void> deleteDraft(@PathVariable("id") UUID id);

    /**
     * Search history items
     *
     * @param id (Optional) Item unique id
     * @param publisher (Optional) Publisher unique id
     * @param deleted (Optional) If <code>true</code> load only deleted items
     * @param status (Optional) Draft item status
     * @param pageIndex The page index. Page index is 1-based
     * @param pageSize The page size
     * @return An instance of {@link CatalogCollection}
     */
    @GetMapping(value = "/api/history/search")
    ResponseEntity<CatalogueResponse<CatalogueCollection>> findAllHistory(
        @RequestParam(name = "item_id", required = false) UUID id,
        @RequestParam(name = "publisher_id", required = false) UUID publisher,
        @RequestParam(name = "deleted", required = false) Boolean deleted,
        @RequestParam(name = "page", defaultValue = "1") int pageIndex,
        @RequestParam(name = "per_page", defaultValue = "10") int pageSize
    );

}
