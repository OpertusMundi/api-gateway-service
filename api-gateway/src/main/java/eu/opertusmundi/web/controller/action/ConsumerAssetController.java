package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
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
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.asset.EnumConsumerSubSortField;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.file.CopyToDriveCommandDto;
import eu.opertusmundi.common.model.openapi.schema.ConsumerEndpointTypes;
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
    name        = EndpointTags.ConsumerAssets,
    description = "API for browsing consumer owned assets and subscriptions"
)
@RequestMapping(path = "/action", produces = MediaType.APPLICATION_JSON_VALUE)
public interface ConsumerAssetController {

    /**
     * Search consumer owned assets
     *
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "consumer-assets-01",
        summary     = "Owned Assets",
        description = "Browse consumer's owned assets. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ConsumerEndpointTypes.AssetCollectionResponse.class)
        )
    )
    @GetMapping(value = "/consumer/assets")
    @Secured({"ROLE_CONSUMER"})
    RestResponse<?> findAllAssets(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Asset type"
        )
        @RequestParam(name = "type", required = false) EnumAssetType type,
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
        @RequestParam(name = "orderBy", defaultValue = "ADDED_ON") EnumConsumerAssetSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Search consumer registered subscriptions
     *
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "consumer-assets-02",
        summary     = "Registered subscriptions",
        description = "Browse consumer's registered subscriptions. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ConsumerEndpointTypes.SubscriptionCollectionResponse.class)
        )
    )
    @GetMapping(value = "/consumer/subscriptions")
    @Secured({"ROLE_CONSUMER"})
    RestResponse<?> findAllSubscriptions(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Service type"
        )
        @RequestParam(name = "type", required = false) EnumSpatialDataServiceType type,
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
        @RequestParam(name = "orderBy", defaultValue = "ADDED_ON") EnumConsumerSubSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

    /**
     * Get a single subscription record
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "consumer-assets-03",
        summary     = "Get subscription",
        description = "Get a single subscription registered to the user's account. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ConsumerEndpointTypes.SubscriptionResponse.class)
        )
    )
    @GetMapping(value = "/consumer/subscriptions/{key}")
    @Secured({"ROLE_CONSUMER"})
    RestResponse<?> findOneSubscription(
        @Parameter(
            in = ParameterIn.PATH,
            description = "Subscription key"
        )
        @PathVariable(name = "key") UUID key
    );

    /**
     * Cancel an active subscription
     *
     * @param key
     * @return
     */
    @Operation(
        operationId = "consumer-assets-04",
        summary     = "Cancel subscription",
        description = "Cancel an active subscription registered to the user's account. Required role: `ROLE_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "204",
        description = "successful operation"
    )
    @DeleteMapping(value = "/consumer/subscriptions/{key}")
    @Secured({"ROLE_CONSUMER"})
    ResponseEntity<Void> cancelSubscription(
        @Parameter(
            in = ParameterIn.PATH,
            description = "Subscription key"
        )
        @PathVariable(name = "key") UUID key
    );

    /**
     * Download a resource file from a purchased asset
     *
     * @param pid
     * @param resourceKey
     * @param response
     * @return
     * @throws IOException
     */
    @Operation(
        operationId = "consumer-assets-05",
        summary     = "Download resource",
        description = "Downloads a resource of an asset, purchased by the authenticated user. Required role: `ROLE_CONSUMER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/consumer/assets/{pid}/resource/{resourceKey}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<StreamingResponseBody> downloadResource(
        @Parameter(
            in = ParameterIn.PATH,
            description = "Asset unique identifier"
        )
        @PathVariable(name = "pid") String pid,
        @Parameter(
            in = ParameterIn.PATH,
            description = "Resource unique identifier"
        )
        @PathVariable(name = "resourceKey") String resourceKey,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;


    /**
     * Copy a resource file of an asset to user Topio drive
     *
     * @param pid
     * @param resourceKey
     * @param command
     * @param validationResult
     * @return
     * @throws IOException
     */
    @Operation(
        operationId = "consumer-assets-06",
        summary     = "Copy resource",
        description = "Copies a resource file of a purchased asset to the user's Topio drive. If the file size is "
                    + "greater than `10MB`, the copy operation is asynchronous and a temporary file is created. "
                    + "The temporary file has the suffix `.copying`. Required role: `ROLE_CONSUMER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ConsumerEndpointTypes.CopyToDriveResponse.class)
        )
    )
    @PostMapping(value = "/consumer/assets/{pid}/resource/{resourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({"ROLE_CONSUMER"})
    BaseResponse copyToDrive(
        @Parameter(
            in = ParameterIn.PATH,
            description = "Asset unique identifier"
        )
        @PathVariable(name = "pid") String pid,
        @Parameter(
            in = ParameterIn.PATH,
            description = "Resource unique identifier"
        )
        @PathVariable(name = "resourceKey") String resourceKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Copy resource command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CopyToDriveCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        CopyToDriveCommandDto command,
        @Parameter(hidden = true)
        BindingResult validationResult
    ) throws IOException;
}
