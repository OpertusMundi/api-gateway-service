package eu.opertusmundi.web.controller.action;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        summary     = "Get file system. Roles required: ROLE_USER",
        description = "List (recursively) files and folders for user's directory",
        tags        = { "File System" },
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
    RestResponse<?> browseDirectory();

    /**
     * Create a new folder
     *
     * @param command A {@link FilePathCommand} command
     * @return the updated file system
     */
    @Operation(
        operationId = "file-system-02",
        summary     = "Create a new folder. Roles required: ROLE_USER",
        description = "Creates a directory in the user's remote file system by creating all nonexistent parent directories first",
        tags        = { "File System" },
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
    RestResponse<?> createFolder(@RequestBody FilePathCommand command);

    /**
     * Download a file
     *
     * @param relativePath File path in user's remote file system
     * @return The requested file
     */
    @Operation(
        operationId = "file-system-04",
        summary     = "Download a file. Roles required: ROLE_USER",
        description = "Downloads a file",
        tags        = { "File System" },
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
    );

    /**
     * Delete a file
     *
     * @param path Path of the file system entry to delete. Can be a folder or a
     *             file. A folder must be empty before deletion
     * @return the updated file system
     */
    @Operation(
        operationId = "file-system-05",
        summary     = "Delete a file or directory. Roles required: ROLE_USER",
        description = "Deletes a file or directory. A directory must be empty before deletion",
        tags        = { "File System" },
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
    );

    /**
     * Uploads a file and links it to the entity of the specified type with the
     * given key
     *
     * @param file An instance of {@link MultipartFile} with the uploaded file.
     * @param command Instance of {@link FileUploadCommand} with file metadata and upload settings.
     * @return the updated file system
     */
    @Operation(
        operationId = "file-system-03",
        summary     = "Upload a file. Roles required: ROLE_USER",
        description = "Uploads a file to the user's remote file system. If the path does not exist, it is created. "
                      + "If the file already exists, the overwite attribute must be set to true, or an error is returned.",
        tags        = { "File System" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileSystemTypes.FileSystemResponse.class))
    )
    @PostMapping(value = "/file-manager/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    RestResponse<?> uploadFile(
        @Parameter(schema = @Schema(
            name = "file", type = "string", format = "binary", description = "Uploaded file"
        ))
        @RequestPart(name = "file", required = true) MultipartFile file,
        @RequestPart(name = "data", required = true) FileUploadCommand command
    );

}
