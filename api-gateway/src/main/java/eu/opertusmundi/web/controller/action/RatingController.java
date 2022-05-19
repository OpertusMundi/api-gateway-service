package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.rating.AssetRatingCommandDto;
import eu.opertusmundi.common.model.rating.RatingDto;
import eu.opertusmundi.common.model.rating.ProviderRatingCommandDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.RatingEndpointTypes;
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
    name        = EndpointTags.Rating,
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
        description = "Get all ratings for a specific asset"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingEndpointTypes.AssetResponse.class))
    )
    @GetMapping(value = "/asset/{id}")
    RestResponse<List<RatingDto>> getAssetRatings(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset unique id"
        )
        @PathVariable(name = "id", required = true) String id
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
        description = "Get all ratings for a specific provider"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = RatingEndpointTypes.ProviderResponse.class))
    )
    @GetMapping(value = "/provider/{id}")
    RestResponse<List<RatingDto>> getProviderRatings(
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
        description = "Adds a new rating for a specific asset. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PostMapping(value = "/asset/{id}")
    @Secured({"ROLE_CONSUMER"})
    @Validated
    BaseResponse addAssetRating(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset unique id"
        )
        @PathVariable(name = "id", required = true)
        String id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Rating command",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetRatingCommandDto.class)),
            required = true
        )
        @Valid @RequestBody(required = true) AssetRatingCommandDto command,
        @Parameter(hidden = true) BindingResult validationResult
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
        description = "Adds a new rating for a specific provider. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PostMapping(value = "/provider/{id}")
    @Secured({"ROLE_CONSUMER"})
    @Validated
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProviderRatingCommandDto.class)),
            required = true
        )
        @Valid @RequestBody(required = true) ProviderRatingCommandDto command,
        @Parameter(hidden = true) BindingResult validationResult
    );

}
