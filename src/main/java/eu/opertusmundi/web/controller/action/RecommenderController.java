package eu.opertusmundi.web.controller.action;

import java.util.Optional;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.web.model.recommender.client.RecommenderClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for querying the recommender system
 */
@Tag(
    name        = "Recommender",
    description = "The recommender API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface RecommenderController {

    /**
     * Get recommended assets for the currently authenticated user
     *
     * @param limit Maximum number of returned assets
     *
     * @return An instance of {@link RecommenderClientResponse} class
     */
    @Operation(
        summary     = "Get recommended assets",
        description = "Get a list of recommended assets for the currently authenticated user.",
        tags        = { "Catalogue", "Recommender" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecommenderClientResponse.class))
    )
    @GetMapping(value = "/recommender", consumes = "application/json")
    @Secured({"ROLE_USER"})
    BaseResponse getRecommendedAssets(
        @Parameter(
            in          = ParameterIn.QUERY,
            description = "Maximum number of recommended assets to return"
        )
        @RequestParam(name = "limit", defaultValue = "10", required = false)
        Optional<Integer> limit
    );

}
