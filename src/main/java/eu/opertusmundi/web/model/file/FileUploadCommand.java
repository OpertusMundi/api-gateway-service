package eu.opertusmundi.web.model.file;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "File upload options")
@Getter
@Setter
public class FileUploadCommand {

    @Schema(description = "File comment", required = false)
    private String comment;

    @Schema(description = "File name", required = true)
    private String filename;

    @Schema(description = "True if an existing file should be overwritten", required = false, defaultValue = "false")
    private boolean overwrite = false;

    @Schema(description = "Absolute path in the user's remote file system where file will be saved", required = true)
    private String path;

    @JsonIgnore
    private long size;

    @JsonIgnore
    private int userId;

}
