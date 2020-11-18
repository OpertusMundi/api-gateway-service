package eu.opertusmundi.web.model.file;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Directory properties")
public class DirectoryDto extends FileSystemEntry {

    private static final long serialVersionUID = 1L;

    private final List<FileDto> files = new ArrayList<FileDto>();

    private final List<DirectoryDto> folders = new ArrayList<DirectoryDto>();

    public DirectoryDto(String name, String path, ZonedDateTime modifiedOn) {
        super(name, path, -1, modifiedOn);
    }

    public DirectoryDto(String name, String path, long modifiedOn) {
        super(name, path, -1, modifiedOn);
    }

    public DirectoryDto(String name, String path, ZonedDateTime modifiedOn, List<FileDto> files, List<DirectoryDto> folders) {
        super(name, path, -1, modifiedOn);
        this.files.addAll(files);
        this.folders.addAll(folders);
    }

    public DirectoryDto(String name, String path, long modifiedOn, List<FileDto> files, List<DirectoryDto> folders) {
        super(name, path, -1, modifiedOn);
        this.files.addAll(files);
        this.folders.addAll(folders);
    }

    public List<FileDto> getFiles() {
        this.files.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));
        return Collections.unmodifiableList(this.files);
    }

    public List<DirectoryDto> getFolders() {
        this.folders.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));
        return Collections.unmodifiableList(this.folders);
    }

    @Schema(description = "Total number of files and folders in this directory")
    public int getCount() {
        return this.files.size() + this.folders.size();
    }

    @Override
    @Schema(description = "Size of all files in this directory (including all child directories)")
    public long getSize() {
        return this.files.stream().mapToLong(f -> f.getSize()).sum() + this.folders.stream().mapToLong(f -> f.getSize()).sum();
    }

    public void addFile(FileDto fi) {
        final Optional<FileDto> existing = this.files.stream()
            .filter(f -> f.getName().equalsIgnoreCase(fi.getName()))
            .findFirst();
        if (existing.isPresent()) {
            this.files.remove(existing.get());
        }
        this.files.add(fi);
    }

    public void addDirectory(DirectoryDto di) {
        final Optional<DirectoryDto> existing = this.folders.stream()
            .filter(d -> d.getName().equalsIgnoreCase(di.getName()))
            .findFirst();
        if (existing.isPresent()) {
            this.folders.remove(existing.get());
        }
        this.folders.add(di);
    }

}
