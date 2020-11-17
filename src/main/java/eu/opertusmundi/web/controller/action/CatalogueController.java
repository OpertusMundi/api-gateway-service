package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueAddItemCommandDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientItemResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueSearchQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for accessing catalogue data
 */
@Tag(
    name        = "Catalogue",
    description = "The catalogue API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface CatalogueController {

    /**
     * Search catalogue items
     *
     * @param request The search criteria
     * @return An instance of {@link CatalogueClientCollectionResponse} class
     */
    @Operation(
        operationId = "catalogue-01",
        summary     = "Search catalogue items",
        description = "Search catalogue items based on one or more criteria. Supports data paging and sorting.",
        tags        = { "Catalogue" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueClientCollectionResponse.class))
    )
    @PostMapping(value = "/catalogue", consumes = "application/json")
    RestResponse<?> find(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Search criteria",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueSearchQuery.class)),
            required = true
        )
        @RequestBody CatalogueSearchQuery query
    );

    /**
     * Get a single catalogue item
     *
     * @param id The item unique id
     * @return A response with a result of type {@link CatalogueItemDto}
     */
    @Operation(
        operationId = "catalogue-02",
        summary     = "Get catalogue item",
        description = "Get a single catalogue item by its unique identifier.",
        tags        = { "Catalogue" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueClientItemResponse.class))
    )
    @GetMapping(value = "/catalogue/items/{id}")
    RestResponse<CatalogueItemDetailsDto> findOne(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = false,
            description = "Item unique id"
        )
        @PathVariable UUID id
    );

    /**
     * Create catalogue item
     *
     * @param request The item to create
     * @return A response with a result of type {@link CatalogueItemDto}
     */
    @Operation(
        operationId = "catalogue-03",
        summary     = "Create",
        description = "Create catalogue item.",
        tags        = { "Catalogue" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PostMapping(value = "/catalogue/items", consumes = "application/json")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New catalogue item.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CatalogueAddItemCommandDto.class)),
            required = true
        )
        @RequestBody CatalogueAddItemCommandDto command
    );
}
