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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.EnumOwningEntityType;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.filemanager.FileUploadCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint that manages uploaded files
 */
@Tag(
    name        = "File Manager",
    description = "The file manager API"
)
@RequestMapping(path = "/action", produces = "application/json")
@Secured({ "ROLE_USER" })
public interface FileManagerController {

    /**
     * Uploads a file and links it to the entity of the specified type with the
     * given key
     *
     * @param file Uploaded file
     * @param command Upload command with file metadata
     * @param type Entity type
     * @param key Entity instance unique key
     */
    @Operation(
        summary     = "Uploads a file. Roles required: ROLE_USER",
        description = "Uploads a file and links it to the entity of the specified type with the given key",
        tags        = { "File Manager" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @PostMapping(value = "/file-manager/{type}/{key}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RestResponse<?> uploadFile(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Entity type"
        )
        @PathVariable(name = "type", required = true) EnumOwningEntityType type,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Entity instance unique key"
        )
        @PathVariable(name = "key", required = true) UUID key,
        @Parameter(
            schema = @Schema(
                name = "file", type = "string", format = "binary", description = "Uploaded file"
            )
        )
        @RequestPart(name = "file", required = true) MultipartFile file,
        @RequestPart(name = "data", required = true) FileUploadCommand command);

    /**
     * Downloads a file
     *
     * @param type Entity type
     * @param key Entity instance unique key
     * @param file File unique key
     */
    @Operation(
        summary     = "Download a file. Roles required: ROLE_USER",
        description = "Downloads a file of the entity of the specified type with the given key",
        tags        = { "File Manager" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @GetMapping(value = "/file-manager/{type}/{key}/{file}")
    public ResponseEntity<StreamingResponseBody> downloadFile(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Entity type"
        )
        @PathVariable(name = "type", required = true) EnumOwningEntityType type,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Entity instance unique key"
        )
        @PathVariable(name = "key", required = true) UUID ownerKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "File unique key"
        )
        @PathVariable(name = "file", required = true) UUID fileKey,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

    /**
     * Deletes a file
     *
     * @param type Entity type
     * @param key Entity instance unique key
     * @param file File unique key
     */
    @Operation(
        summary     = "Delete a file. Roles required: ROLE_USER",
        description = "Deletes a file of the entity of the specified type with the given key",
        tags        = { "File Manager" },
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @DeleteMapping(value = "/file-manager/{type}/{key}/{file}")
    public RestResponse<?> deleteFile(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Entity type"
        )
        @PathVariable(name = "type", required = true) EnumOwningEntityType type,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Entity instance unique key"
        )
        @PathVariable(name = "key", required = true) UUID key,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "File unique key"
        )
        @PathVariable(name = "file", required = true) UUID file);

}
