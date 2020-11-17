package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.openapi.schema.RatingEndpointTypes;
import eu.opertusmundi.web.model.rating.client.ClientRatingCommandDto;
import eu.opertusmundi.web.model.rating.client.ClientRatingDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for managing user shopping cart
 */
@Tag(
    name        = "Rating",
    description = "The rating API"
)
@RequestMapping(path = "/action/rating", produces = "application/json")
public interface RatingController {

    /**
     * Get ratings for a single asset
     *
     * @param id Asset unique id
     *
     * @return An instance of {@link RatingEndpointTypes.AssetResponse}
     */
    @Operation(
        operationId = "rating-01",
        summary     = "Get asset ratings",
        description = "Get all ratings for a specific asset",
        tags        = { "Rating" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingEndpointTypes.AssetResponse.class))
    )
    @GetMapping(value = "/asset/{id}")
    RestResponse<List<ClientRatingDto>> getAssetRatings(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset unique id"
        )
        @PathVariable(name = "id", required = true) UUID id
    );


    /**
     * Get ratings for a single provider
     *
     * @param id Provider unique id
     *
     * @return An instance of {@link RatingEndpointTypes.ProviderResponse}
     */
    @Operation(
        operationId = "rating-03",
        summary     = "Get provider ratings",
        description = "Get all ratings for a specific provider",
        tags        = { "Rating" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingEndpointTypes.ProviderResponse.class))
    )
    @GetMapping(value = "/provider/{id}")
    RestResponse<List<ClientRatingDto>> getProviderRatings(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Provider unique id"
        )
        @PathVariable(name = "id", required = true) UUID id
    );

    /**
     * Add asset rating
     *
     * @param id Asset unique id
     * @param command The command object for adding a new rating for an asset
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "rating-02",
        summary     = "Add asset rating",
        description = "Adds a new rating for a specific asset",
        tags        = { "Rating" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PostMapping(value = "/asset/{id}")
    @Secured({"ROLE_USER"})
    BaseResponse addAssetRating(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset unique id"
        )
        @PathVariable(name = "id", required = true)
        UUID id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Rating command",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientRatingCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) ClientRatingCommandDto command
    );

    /**
     * Add provider rating
     *
     * @param id Provider unique id
     * @param command The command object for adding a new rating for a provider
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "rating-03",
        summary     = "Add provider rating",
        description = "Adds a new rating for a specific provider",
        tags        = { "Rating" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PostMapping(value = "/provider/{id}")
    @Secured({"ROLE_USER"})
    BaseResponse addProviderRating(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Provider unique id"
        )
        @PathVariable(name = "id", required = true)
        UUID id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Rating command",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientRatingCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) ClientRatingCommandDto command
    );

}
