package eu.opertusmundi.web.controller.action;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileUploadCommand;
import eu.opertusmundi.web.model.openapi.schema.FileSystemTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * File system actions
 */
@Tag(
    name        = "File System",
    description = "The user file system API"
)
@RequestMapping(path = "/action", produces = MediaType.APPLICATION_JSON_VALUE)
@Secured({"ROLE_USER"})
public interface FileSystemController {

    /**
     * List (recursively) files and folders for user's directory
     *
     * @return information on all files and folders
     */
    @Operation(
        operationId = "file-system-01",
        summary     = "Get file system",
        description = "List (recursively) files and folders for user's directory. Roles required: <b>ROLE_USER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileSystemTypes.FileSystemResponse.class))
    )
    @GetMapping(value = "/file-system")
    RestResponse<?> browseDirectory() throws AccessDeniedException;

    /**
     * Create a new folder
     *
     * @param command A {@link FilePathCommand} command
     * @return the updated file system
     */
    @Operation(
        operationId = "file-system-02",
        summary     = "Create new folder",
        description = "Creates a directory in the user's remote file system by creating all nonexistent parent directories first."
                    + " Roles required: <b>ROLE_USER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileSystemTypes.FileSystemResponse.class))
    )
    @PostMapping(value = "/file-system/folders")
    RestResponse<?> createFolder(@RequestBody FilePathCommand command) throws AccessDeniedException;

    /**
     * Download a file
     *
     * @param relativePath File path in user's remote file system
     * @return The requested file
     */
    @Operation(
        operationId = "file-system-04",
        summary     = "Download file",
        description = "Downloads a file. Roles required: <b>ROLE_USER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
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

    /**
     * Delete a file
     *
     * @param path Path of the file system entry to delete. Can be a folder or a
     *             file. A folder must be empty before deletion
     * @return the updated file system
     */
    @Operation(
        operationId = "file-system-05",
        summary     = "Delete file or directory",
        description = "Deletes a file or directory. A directory must be empty before deletion. Roles required: <b>ROLE_USER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileSystemTypes.FileSystemResponse.class))
    )
    @DeleteMapping(value = "/file-system", params = { "path" })
    public RestResponse<?> deletePath(
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = true,
            description = "Relative path in user's remote file system"
        )
        @RequestParam(name = "path", required = true) String path
    ) throws AccessDeniedException;

    /**
     * Uploads a file to user's remote file system
     *
     * @param file An instance of {@link MultipartFile} with the uploaded file.
     * @param command Instance of {@link FileUploadCommand} with file metadata and upload settings.
     * @return the updated file system
     */
    @Operation(
        operationId = "file-system-03",
        summary     = "Upload file",
        description = "Uploads a file to the user's remote file system. If the path does not exist, it is created. "
                      + "If the file already exists, the overwrite attribute must be set to true, or an error is returned. "
                      + "Roles required: <b>ROLE_USER</b>",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileSystemTypes.FileSystemResponse.class))
    )
    @PostMapping(value = "/file-system/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    RestResponse<?> uploadFile(
        @Parameter(schema = @Schema(
            name = "file", type = "string", format = "binary", description = "Uploaded file"
        ))
        @RequestPart(name = "file", required = true) MultipartFile file,
        @RequestPart(name = "data", required = true) FileUploadCommand command
    ) throws IOException;

}
