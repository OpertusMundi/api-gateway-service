package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.openapi.schema.CatalogueEndpointTypes;
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
    name        = EndpointTags.DraftReview,
    description = "The Helpdesk draft review API"
)
@RequestMapping(path = "/action", produces = "application/json")
@Secured({"ROLE_HELPDESK"})
public interface HelpdeskDraftAssetController {

    /**
     * Get a single catalogue draft item as a published item
     *
     * @param draftKey The item unique key
     * @return A response with a result of type {@link CatalogueItemDetailsDto}
     */
    @Operation(
        operationId = "helpdesk-draft-asset-01",
        summary     = "Get draft",
        description = "Get a single catalogue draft item by its unique identifier. "
                    + "The draft is returned as if it has been published to the catalogue. "
                    + "All automated metadata is displayed (including hidden ones). "
                    + "Required roles: <b>ROLE_HELPDESK</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = CatalogueEndpointTypes.ItemResponse.class)
        )
    )
    @GetMapping(value = "/helpdesk-drafts/{draftKey}")
    RestResponse<CatalogueItemDetailsDto> findOneDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique key"
        )
        @PathVariable UUID draftKey
    );

    /**
     * Download an additional resource file
     *
     * @param draftKey Draft unique key
     * @param resourceKey Resource unique key
     *
     * @return The requested file
     */
    @Operation(
        operationId = "helpdesk-draft-asset-02",
        summary     = "Download additional resource",
        description = "Downloads an additional resource file. Roles required: <b>ROLE_HELPDESK</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(
        value = "/helpdesk-drafts/{draftKey}/additional-resources/{resourceKey}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
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
     * @param draftKey Draft unique key
     * @param resourceKey Resource unique key
     * @param propertyName The property name
     *
     * @return The requested property value
     */
    @Operation(
        operationId = "helpdesk-draft-asset-03",
        summary     = "Get metadata property",
        description = "Gets metadata property value for the specified resource file. Roles required: <b>ROLE_HELPDESK</b>",
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
        value = "/helpdesk-drafts/{draftKey}/resources/{resourceKey}/metadata/{propertyName}",
        produces = {MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    ResponseEntity<StreamingResponseBody> getMetadataProperty(
        @Parameter(
            in          = ParameterIn.PATH,
            description = "Draft unique key"
        )
        @PathVariable UUID draftKey,
        @Parameter(
            in          = ParameterIn.PATH,
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

}
