package eu.opertusmundi.web.controller.api;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.openapi.schema.CatalogueEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = EndpointTags.API_Catalogue)
@SecurityRequirement(name = "jwt")
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Secured({"ROLE_API"})
public interface CatalogueController {

    /**
     * Search catalogue published items
     *
     * @param request The search criteria
     * @return An instance of {@link CatalogueClientCollectionResponse} class
     */
    @Operation(
        operationId = "api-catalogue-01",
        summary     = "Find",
        description = "Search catalogue published items based on one or more criteria using Elasticsearch."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemCollectionResponse.class)
        )
    )
    @GetMapping(value = "/catalogue")
    RestResponse<?> findAll(
        @ParameterObject()
        ElasticAssetQuery query
    );

    /**
     * Get a single catalogue item
     *
     * @param id The item unique id
     * @return A response with a result of type {@link CatalogueItemDto}
     */
    @Operation(
        operationId = "api-catalogue-02",
        summary     = "Get asset by id",
        description = "Get a single catalogue item by its unique identifier."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CatalogueEndpointTypes.ItemResponse.class)
        )
    )
    @GetMapping(value = "/catalogue/items/{id}")
    RestResponse<CatalogueItemDetailsDto> findOne(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable String id
    );

}
