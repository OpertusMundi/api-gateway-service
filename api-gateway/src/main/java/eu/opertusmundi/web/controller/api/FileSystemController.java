package eu.opertusmundi.web.controller.api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.FileSystemEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.API_FileSystem,
    description = "The user file system API"
)
@SecurityRequirement(name = "jwt")
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Secured({"ROLE_API"})
public interface FileSystemController {

    /**
     * List (recursively) files and folders for user's directory
     *
     * @return information on all files and folders
     */
    @Operation(
        operationId = "api-file-system-01",
        summary     = "Get file system",
        description = "List (recursively) files and folders for user's directory."
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileSystemEndpointTypes.FileSystemResponse.class))
    )
    @GetMapping(value = "/file-system")
    RestResponse<?> browseDirectory() throws AccessDeniedException;

    /**
     * Download a file
     *
     * @param relativePath File path in user's remote file system
     * @return The requested file
     */
    @Operation(
        operationId = "api-file-system-02",
        summary     = "Download file",
        description = "Downloads a file from user's file system."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested file"))
    )
    @GetMapping(value = "/file-system/files", params = { "path" }, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<StreamingResponseBody> downloadFile(
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = true,
            description = "File path in user's remote file system"
        )
        @RequestParam(name = "path", required = true) String relativePath,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

}
