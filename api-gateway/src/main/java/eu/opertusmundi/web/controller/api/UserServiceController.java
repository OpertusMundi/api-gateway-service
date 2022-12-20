package eu.opertusmundi.web.controller.api;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceSortField;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceType;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.openapi.schema.UserServiceEndpointTypes;
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
    name        = EndpointTags.API_UserServices,
    description = "User private service publication API"
)
@SecurityRequirement(name = "jwt")
@RequestMapping(path = "/api/user/services", produces = MediaType.APPLICATION_JSON_VALUE)
@Secured({"ROLE_API"})
public interface UserServiceController {

    /**
     * Search user services
     *
     * @param status Service status
     * @param pageIndex Page index
     * @param pageSize Page size
     *
     * @return An instance of {@link UserServiceEndpointTypes.ServiceCollectionResponse} class
     */
    @Operation(
        operationId = "api-user-service-01",
        summary     = "Find All",
        description = "Find user services based on one or more criteria."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = UserServiceEndpointTypes.ServiceCollectionResponse.class)
        )
    )
    @GetMapping(value = "")
    RestResponse<?> findAll(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Service type",
            example = "WMS"
        )
        @RequestParam(name = "serviceType", required = false) Set<EnumUserServiceType> serviceType,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0") int pageIndex,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "10") int pageSize,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order by property"
        )
        @RequestParam(name = "orderBy", defaultValue = "UPDATED_ON") EnumUserServiceSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Get a single service
     *
     * @param key The service unique key
     * @return A response with a result of type {@link UserServiceDto}
     */
    @Operation(
        operationId = "api-user-service-02",
        summary     = "Find One",
        description = "Find one service by its unique identifier."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserServiceEndpointTypes.ServiceResponse.class))
    )
    @GetMapping(value = "/{serviceKey}")
    RestResponse<UserServiceDto> findOne(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Service unique key"
        )
        @PathVariable UUID serviceKey
    );

}
