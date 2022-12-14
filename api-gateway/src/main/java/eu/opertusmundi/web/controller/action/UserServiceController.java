package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceSortField;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceStatus;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceType;
import eu.opertusmundi.common.model.asset.service.UserServiceCommandDto;
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
    name        = EndpointTags.UserServices,
    description = "User private service publication API"
)
@RequestMapping(path = "/action/user/services", produces = "application/json")
@Secured({"ROLE_CONSUMER", "ROLE_VENDOR_CONSUMER"})
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
        operationId = "user-service-01",
        summary     = "Find All",
        description = "Find user services based on one or more criteria. "
                    + "Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
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
            description = "Service status",
            example = "PUBLISHED"
        )
        @RequestParam(name = "status", required = false) Set<EnumUserServiceStatus> status,
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
        operationId = "user-service-02",
        summary     = "Find One",
        description = "Find one service by its unique identifier. "
                    + "Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
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

    /**
     * Create a user service from a file in the user's file system
     *
     * @param command
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "assets-03",
        summary     = "Create",
        description = "Create a user service from a file in user's file system. "
                    + "Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserServiceEndpointTypes.ServiceResponse.class))
    )
    @PostMapping(value = "")
    @Validated
    RestResponse<UserServiceDto> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User service creation command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserServiceCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        UserServiceCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Delete service
     *
     * @param serviceKey The service unique key
     * @return
     */
    @Operation(
        operationId = "user-service-04",
        summary     = "Delete",
        description = "Delete a user service. If the record is locked by another user, the operation will fail. "
                    + "Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/{serviceKey}")
    BaseResponse delete(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Service unique key"
        )
        @PathVariable UUID serviceKey
    );

    /**
     * Get metadata property value
     *
     * @param serviceKey Service unique key
     * @param resourceKey Resource unique key
     * @param propertyName The property name
     *
     * @return The requested property value
     */
    @Operation(
        operationId = "user-service-05",
        summary     = "Get metadata property",
        description = "Gets metadata property value for the specified resource file. Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested value"))
    )
    @GetMapping(
        value = "/{serviceKey}/metadata/{propertyName}",
        produces = {MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    ResponseEntity<StreamingResponseBody> getMetadataProperty(
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Service unique key"
        )
        @PathVariable UUID serviceKey,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Property name"
        )
        @PathVariable String propertyName,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

}
