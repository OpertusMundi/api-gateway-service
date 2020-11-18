package eu.opertusmundi.web.model.file;

import eu.opertusmundi.common.model.FileSystemMessageCode;
import lombok.Getter;

public class FileSystemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final FileSystemMessageCode code;

    public FileSystemException(FileSystemMessageCode code) {
        super("An I/O error has occurred");

        this.code = code;
    }

    public FileSystemException(String message) {
        super(message);

        this.code = FileSystemMessageCode.IO_ERROR;
    }

    public FileSystemException(FileSystemMessageCode code, String message) {
        super(message);

        this.code = code;
    }

    public FileSystemException(FileSystemMessageCode code, String message, Throwable cause) {
        super(message, cause);

        this.code = code;
    }

}