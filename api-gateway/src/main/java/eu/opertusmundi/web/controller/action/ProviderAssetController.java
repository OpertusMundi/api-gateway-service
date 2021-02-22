package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
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
    name        = EndpointTags.Asset,
    description = "The asset API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface ProviderAssetController {

    /**
     * Download an additional resource file
     *
     * @param pid Asset persistent identifier (PID)
     * @param resourceKey Resource unique key
     * 
     * @return The requested file
     */
    @Operation(
        operationId = "assets-01",
        summary     = "Download additional resource",
        description = "Downloads an additional resource file"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/assets/{pid}/additional-resources/{resourceKey}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset pid"
        )
        @PathVariable String pid,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Resource unique key"
        )
        @PathVariable UUID resourceKey,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;
    
    /**
     * Get metadata property value
     *
     * @param pid Asset persistent identifier (PID)
     * @param resourceKey Resource unique key
     * @param propertyName The property name
     * 
     * @return The requested property value
     */
    @Operation(
        operationId = "assets-02",
        summary     = "Get metadata property",
        description = "Gets metadata property value for the specified resource file. Roles required: <b>ROLE_USER</b>",
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
        value = "/assets/{pid}/resources/{resourceKey}/metadata/{propertyName}", 
        produces = {MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    @Secured({"ROLE_USER"})
    ResponseEntity<StreamingResponseBody> getMetadataProperty(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Asset pid"
        )
        @PathVariable String pid,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Resource unique key"
        )
        @PathVariable UUID resourceKey,
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Property name"
        )
        @PathVariable String propertyName,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;
    
    /**
     * Delete catalogue item
     *
     * @param id The item unique id
     * @return
     */
    @Operation(
        operationId = "assets-03",
        summary     = "Delete asset",
        description = "Delete asset from catalogue. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/assets/{id}")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse deleteAsset(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable UUID id
    );

}
