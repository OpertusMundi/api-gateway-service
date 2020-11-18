package eu.opertusmundi.web.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import eu.opertusmundi.web.model.file.DirectoryDto;

public interface DirectoryTraverse
{
    /**
     * @see {@link DirectoryTraverse#getDirectoryInfo(Path, int)}
     */
    DirectoryDto getDirectoryInfo(Path rootDir) throws IOException;

    /**
     * Traverse directory entries (recursively) and collect detailed information on
     * file-system entries (files and nested directories).
     *
     * @param rootDir The root directory of this traversal
     * @param maxDepth A maximum depth to descend
     * @throws IOException
     */
    DirectoryDto getDirectoryInfo(Path rootDir, int maxDepth) throws IOException;

    /**
     * Traverse directory entries (recursively) and collect detailed information on
     * file-system entries (files and nested directories).
     *
     * @param rootDir The root directory of this traversal
     * @param exclude List of folder names to exclude
     * @throws IOException
     */
    DirectoryDto getDirectoryInfo(Path rootDir, List<String> exclude) throws IOException;

}