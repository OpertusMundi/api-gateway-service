package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.favorite.EnumFavoriteSortField;
import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteAssetCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteProviderCommandDto;
import eu.opertusmundi.common.model.openapi.schema.FavoriteEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.Favorites,
    description = "The consumer favorite API"
)
@RequestMapping(path = "/action/favorites", produces = "application/json")
@Secured({ "ROLE_CONSUMER" })
public interface ConsumerFavoriteController {

    /**
     * Search consumer favorites
     *
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "consumer-favorites-01",
        summary     = "Find",
        description = "Browse consumer's favorite assets and providers. Required roles: <b>ROLE_CONSUMER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = FavoriteEndpointTypes.FavoriteCollectionResponse.class)
        )
    )
    @GetMapping(value = "")
    RestResponse<?> findAll(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Favorite type"
        )
        @RequestParam(name = "type", required = false) EnumFavoriteType type,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0") int pageIndex,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "10") int pageSize,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order by property"
        )
        @RequestParam(name = "orderBy", defaultValue = "CREATED_ON") EnumFavoriteSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Add new favorite
     *
     * @param command
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "consumer-favorites-02",
        summary     = "Add",
        description = "Adds a new favorite for an asset or provider. If a record already exists, "
                    + "the existing record is returned. Roles required: <b>ROLE_CONSUMER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = FavoriteEndpointTypes.FavoriteItemResponse.class)
        )
    )
    @PostMapping(value = "", consumes = { "application/json" })
    @Validated
    RestResponse<?> addFavorite(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Favorite add command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(oneOf = {FavoriteAssetCommandDto.class, FavoriteProviderCommandDto.class})
            ),
            required = true
        )
        @Valid
        @RequestBody
        FavoriteCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Remove existing favorite
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "consumer-favorites-03",
        summary     = "Remove",
        description = "Deletes a favorite for an asset or provider. Roles required: <b>ROLE_CONSUMER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @DeleteMapping(value = "{key}")
    BaseResponse removeFavorite(
        @Parameter(
            in = ParameterIn.PATH,
            required = true,
            description = "Favorite key"
        )
        @PathVariable(name = "key") UUID key
    );
}
