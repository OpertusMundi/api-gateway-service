package eu.opertusmundi.web.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.web.model.file.DirectoryDto;
import eu.opertusmundi.web.model.file.FileDto;

@Service
public class DefaultDirectoryTraverse implements DirectoryTraverse {

    private static final int MAX_DEPTH = 8;

    @Override
    public DirectoryDto getDirectoryInfo(Path rootDir) throws IOException {
        return this.getDirectoryInfo(rootDir, MAX_DEPTH);
    }

    @Override
    public DirectoryDto getDirectoryInfo(Path rootDir, int maxDepth) throws IOException {
        Assert.notNull(rootDir, "A path is required");
        Assert.isTrue(rootDir.isAbsolute(), "The directory is expected as an absolute path");
        Assert.isTrue(Files.isDirectory(rootDir), "The given path is not a directory");
        Assert.isTrue(maxDepth > 0, "The maximum depth must be a positive number");

        return this.createDirectoryInfo("/", rootDir, Paths.get("/"), maxDepth, new ArrayList<String>());
    }

    @Override
    public DirectoryDto getDirectoryInfo(Path rootDir, List<String> exclude) throws IOException {
        Assert.notNull(rootDir, "A path is required");
        Assert.isTrue(rootDir.isAbsolute(), "The directory is expected as an absolute path");
        Assert.isTrue(Files.isDirectory(rootDir), "The given path is not a directory");
        Assert.notNull(exclude, "A exclude is required");

        return this.createDirectoryInfo("/", rootDir, Paths.get("/"), MAX_DEPTH, exclude);
    }

    private DirectoryDto createDirectoryInfo(String name, Path dir, Path relativePath, int depth, List<String> exclude) {
        if (exclude.contains(name)) {
            return null;
        }

        final File         dirAsFile = dir.toFile();
        final DirectoryDto di        = new DirectoryDto(name, relativePath.toString(), dirAsFile.lastModified());

        for (final File entry : dirAsFile.listFiles()) {
            final Path relativeEntryPath = relativePath.resolve(entry.getName());
            if (entry.isDirectory() && !exclude.contains(entry.getName())) {
                if (depth > 0) {
                    // Descend
                    di.addDirectory(this.createDirectoryInfo(entry.getName(), entry.toPath(), relativeEntryPath, depth - 1, exclude));
                } else {
                    // No more recursion is allowed: simply report a directory
                    // entry
                    di.addDirectory(new DirectoryDto(entry.getName(), entry.getPath(), entry.lastModified()));
                }
            } else if (entry.isFile()) {
                di.addFile(new FileDto(entry.getName(), relativeEntryPath.toString(), entry.length(), entry.lastModified()));
            }
        }

        return di;
    }

}