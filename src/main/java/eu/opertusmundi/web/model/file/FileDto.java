package eu.opertusmundi.web.model.file;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;

/**
 * A file system entry for a file
 */
public class FileDto extends FileSystemEntry {

    private static final long serialVersionUID = 1L;

    public FileDto(String name, String path, long size, ZonedDateTime modifiedOn) {
        super(name, path, size, modifiedOn);
    }

    public FileDto(String name, String path, long size, long modifiedOn) {
        super(name, path, size, modifiedOn);
    }

    public FileDto(String name, String path, BasicFileAttributes attrs) {
        super(name, path, attrs);
    }

    public FileDto(Path path, long size, long modifiedOn) {
        this(path.getFileName().toString(), path.toString(), size, modifiedOn);
    }

    public FileDto(Path path, BasicFileAttributes attrs) {
        this(path.getFileName().toString(), path.toString(), attrs);
    }
}