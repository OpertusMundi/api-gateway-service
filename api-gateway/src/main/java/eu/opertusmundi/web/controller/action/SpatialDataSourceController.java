package eu.opertusmundi.web.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.SpatialDataEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.SpatialData,
    description = "The spatial data source API"
)
@RequestMapping(path = "/action/spatial", produces = "application/json")
public interface SpatialDataSourceController {

    /**
     * Get a list of regions
     *
     * @param codes NUTS codes
     *
     * @return An instance of {@link SpatialDataEndpointTypes#RegionCollectionResponse} class
     */
    @Operation(
        operationId = "spatial-nuts-01",
        summary     = "Get regions",
        description = "Gets a feature collection of regions specified by the given NUTS codes"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = SpatialDataEndpointTypes.RegionCollectionResponse.class)
        )
    )
    @GetMapping(value = "/nuts")
    RestResponse<?> findAllByCode(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "A comma separated lits of NUTS codes"
        )
        @RequestParam(name = "code") String codes[]
    );
    
    /**
     * Get a region
     *
     * @param code NUTS code
     *
     * @return An instance of {@link SpatialDataEndpointTypes#RegionResponse} class
     */
    @Operation(
        operationId = "spatial-nuts-02",
        summary     = "Get region",
        description = "Gets the feature of the region specified by the given NUTS code"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = SpatialDataEndpointTypes.RegionResponse.class)
        )
    )
    @GetMapping(value = "/nuts/{code}")
    RestResponse<?> findOneByCode(
        @Parameter(
            in = ParameterIn.PATH,
            required = true,
            description = "A NUTS code"
        )
        @PathVariable(name = "code") String code
    );
    
    /**
     * Search regions by name
     *
     * @param level Filter by level
     * @param query Filter by name
     *
     * @return An instance of {@link SpatialDataEndpointTypes#AutoCompleteRegionResponse} class
     */
    @Operation(
        operationId = "spatial-nuts-03",
        summary     = "Find by name",
        description = "Searches the NUTS regions of the specified level based on their name. "
                    + "Both local (`name`) and latin (`nameLatin`) fields are searched"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = SpatialDataEndpointTypes.AutoCompleteRegionResponse.class)
        )
    )
    @GetMapping(value = "/nuts/{level}/auto-complete")
    RestResponse<?> findAllByName(
        @Parameter(
            in = ParameterIn.PATH,
            required = true,
            description = "NUTS level"
        )
        @PathVariable(name = "level") Long level,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "The name of the region"
        )
        @RequestParam(name = "query") String query
    );

    /**
     * NUTS region WFS
     *
     * @param level NUTS level
     * @param bbox A bounding box
     */
    @Operation(
        operationId = "spatial-nuts-04",
        summary     = "WFS service",
        description = "Implements a pseudo-WFS service for each level of the NUTS regions. "
                    + "All WFS parameters except for the `bbox` are ignored. The CRS is always set to`EPSG:4326`. "
                    + "Optionally, the response may not included the feature geometries"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = SpatialDataEndpointTypes.WfsResponse.class)
        )
    )
    @GetMapping(value = "/nuts/{level}/wfs")
    void wfs(
        @Parameter(
            in = ParameterIn.PATH,
            required = true,
            description = "The NUTS level"
        )
        @PathVariable(name = "level") Integer level,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "A bounding box in EPSG:4326"
        )
        @RequestParam(name = "bbox") String bbox,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "True if geometries must be included in the response"
        )
        @RequestParam(name = "includeGeometry", required = false, defaultValue = "true") boolean includeGeometry,
        @Parameter(hidden = true)
        HttpServletRequest request,
        @Parameter(hidden = true)
        HttpServletResponse response
    );

}
