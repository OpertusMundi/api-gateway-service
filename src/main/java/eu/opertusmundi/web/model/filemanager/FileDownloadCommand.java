package eu.opertusmundi.web.model.filemanager;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumOwningEntityType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDownloadCommand {

    @JsonIgnore
    private UUID fileKey;

    @JsonIgnore
    private UUID owningEntityKey;

    @JsonIgnore
    private EnumOwningEntityType owningEntityType;

    @JsonIgnore
    private UUID userKey;

}
