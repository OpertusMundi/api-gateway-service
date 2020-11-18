package eu.opertusmundi.web.model.file;

import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a file system entry. An entry can be either a file or a directory
 */
public abstract class FileSystemEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "File size")
    private final Long size;

    @Schema(description = "Absolute path in the remote file system")
    private final String path;

    @Schema(description = "File/Directory name")
    private final String name;

    private final ZonedDateTime modifiedOn;

    protected FileSystemEntry(String name, String path, long size, ZonedDateTime modifiedOn) {
        this.name       = name;
        this.path       = path;
        this.size       = size;
        this.modifiedOn = modifiedOn;
    }

    protected FileSystemEntry(String name, String path, long size, long modifiedOn) {
        this.name = name;
        this.path = path;
        this.size = size;

        final Instant t = Instant.ofEpochMilli(modifiedOn);
        this.modifiedOn = ZonedDateTime.ofInstant(t, ZoneOffset.UTC);
    }

    protected FileSystemEntry(String name, String path, BasicFileAttributes attrs) {
        this.name = name;
        this.path = path;
        this.size = attrs.size();

        final Instant t = attrs.lastModifiedTime().toInstant();
        this.modifiedOn = ZonedDateTime.ofInstant(t, ZoneOffset.UTC);
    }

    public long getSize() {
        return this.size;
    }

    public String getPath() {
        return StringUtils.isBlank(this.path) ? "/" : this.path;
    }

    public String getName() {
        return this.name;
    }

    public ZonedDateTime getModified() {
        return this.modifiedOn;
    }

}