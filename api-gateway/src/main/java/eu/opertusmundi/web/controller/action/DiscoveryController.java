package eu.opertusmundi.web.controller.action;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.discovery.client.ClientJoinableResultDto;
import eu.opertusmundi.common.model.discovery.client.ClientRelatedResultDto;
import eu.opertusmundi.common.model.openapi.schema.DiscoveryEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = EndpointTags.Discovery)
@RequestMapping(path = "/action/discovery", produces = MediaType.APPLICATION_JSON_VALUE)
public interface DiscoveryController {

    /**
     * Gets all assets that are joinable with the given source asset
     *
     * @param id The item unique id
     * @return A response with a result of type {@link ClientJoinableResultDto}
     */
    @Operation(
        operationId = "discovery-01",
        summary     = "Get Joinable",
        description = "Gets all assets that are joinable with the given source asset."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = DiscoveryEndpointTypes.JoinableResponse.class)
        )
    )
    @GetMapping(value = "/joinable/{id}")
    RestResponse<ClientJoinableResultDto> findJoinable(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable String id
    );


    /**
     * Get all the assets on the path connecting the source and the target tables
     *
     * @param source The id of the asset to get the table from as source
     * @param target The id of the asset to get the table from as target
     * @return
     */
    @Operation(
        operationId = "discovery-02",
        summary     = "Get Related",
        description = "Get all the assets on the path connecting the source and the target tables."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = DiscoveryEndpointTypes.RelatedResponse.class)
        )
    )
    @GetMapping(value = "/related/{source}")
    RestResponse<ClientRelatedResultDto> findRelated(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "The id of the asset to get the table from as source"
        )
        @PathVariable String source,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = true,
            description = "The id of the asset to get the table from as target"
        )
        @RequestParam String[] target
    );
}
