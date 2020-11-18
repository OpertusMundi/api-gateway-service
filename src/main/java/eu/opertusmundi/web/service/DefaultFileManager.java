package eu.opertusmundi.web.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.FileSystemMessageCode;
import eu.opertusmundi.web.model.file.DirectoryDto;
import eu.opertusmundi.web.model.file.FilePathCommand;
import eu.opertusmundi.web.model.file.FileSystemException;
import eu.opertusmundi.web.model.file.FileUploadCommand;

@Service
public class DefaultFileManager implements FileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFileManager.class);

    private long maxUserSpace;

    @Value("${opertus-mundi.file-system.user-max-space:20971520}")
    private void setMaxUserSpace(String maxUserSpace) {
        this.maxUserSpace = this.parseSize(maxUserSpace);
    }

    @Autowired
    private DefaultUserFileNamingStrategy fileNamingStrategy;

    @Autowired
    private DirectoryTraverse directoryTraverse;

    @Override
    public DirectoryDto browse(FilePathCommand command) throws FileSystemException {
        try {
            final Path userDir = this.fileNamingStrategy.getUserDir(command.getUserId(), true);
            final Path target  = Paths.get(userDir.toString(), command.getPath());

            return this.directoryTraverse.getDirectoryInfo(target);
        } catch (final Exception ex) {
            logger.error("[FileSystem] Failed to load files", ex);

            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    @Override
    public void createPath(FilePathCommand command) throws FileSystemException {
        try {
            if (StringUtils.isEmpty(command.getPath())) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_EMPTY, "A path is required");
            }

            final Path userDir = this.fileNamingStrategy.getUserDir(command.getUserId());

            final Path dir = Paths.get(userDir.toString(), command.getPath());
            if (Files.exists(dir)) {
                throw new FileSystemException(
                    FileSystemMessageCode.PATH_ALREADY_EXISTS,
                    String.format("Directory [%s] already exists", command.getPath())
                );
            }

            Files.createDirectories(dir);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[FileSystem] Failed to create path {} for user {}", command.getPath(), command.getUserId(), ex);

            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    @Override
    public void uploadFile(InputStream input, FileUploadCommand command) throws FileSystemException {
        try  {
            final Path         userDir     = this.fileNamingStrategy.getUserDir(command.getUserId());
            final DirectoryDto userDirInfo = this.directoryTraverse.getDirectoryInfo(userDir);

            final long size = userDirInfo.getSize();
            if (size + command.getSize() > this.maxUserSpace) {
                throw new FileSystemException(FileSystemMessageCode.NOT_ENOUGH_SPACE, "Insufficient storage space");
            }

            if (StringUtils.isBlank(command.getPath())) {
                command.setPath("/");
            }
            if (StringUtils.isEmpty(command.getFilename())) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_EMPTY, "File name is not set");
            }

            final Path relativePath = Paths.get(command.getPath(), command.getFilename());
            final Path absolutePath = this.fileNamingStrategy.resolvePath(command.getUserId(), relativePath);
            final File localFile    = absolutePath.toFile();

            if (localFile.isDirectory()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_DIRECTORY, "File is a directory");
            }

            final String localFolder = localFile.getParent();

            if (!StringUtils.isBlank(localFolder)) {
                Files.createDirectories(Paths.get(localFolder));
            }

            if (localFile.exists()) {
                if (command.isOverwrite()) {
                    FileUtils.deleteQuietly(localFile);
                } else {
                    throw new FileSystemException(FileSystemMessageCode.PATH_ALREADY_EXISTS, "File with the same name already exists");
                }
            }

            Files.copy(input, absolutePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    @Override
    public void deletePath(FilePathCommand command) throws FileSystemException {
        try {
            if (StringUtils.isEmpty(command.getPath())) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_EMPTY, "A path is required");
            }

            final Path userDir      = this.fileNamingStrategy.getUserDir(command.getUserId());
            final Path absolutePath = Paths.get(userDir.toString(), command.getPath());
            final File file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "Path does not exist");
            }
            if (file.isDirectory() && file.listFiles().length != 0) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_EMPTY, "Path is not empty");
            }

            Files.delete(absolutePath);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    @Override
    public Path resolveFilePath(FilePathCommand command) throws FileSystemException {
        try {
            if (StringUtils.isEmpty(command.getPath())) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_EMPTY, "A path to the file is required");
            }

            final Path userDir      = this.fileNamingStrategy.getUserDir(command.getUserId());
            final Path absolutePath = Paths.get(userDir.toString(), command.getPath().toString());
            final File file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "File does not exist");
            } else if (file.isDirectory()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_DIRECTORY, "Path is not a file");
            }

            return file.toPath();
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("[FileSystem] Failed to resolve path [{}] for user [{}]", command.getPath(), command.getUserId());

            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    private long parseSize(String size) {
        Assert.hasText(size, "Size must not be empty");

        size = size.toUpperCase(Locale.ENGLISH);
        if (size.endsWith("KB")) {
            return Long.valueOf(size.substring(0, size.length() - 2)) * 1024;
        }
        if (size.endsWith("MB")) {
            return Long.valueOf(size.substring(0, size.length() - 2)) * 1024 * 1024;
        }
        if (size.endsWith("GB")) {
            return Long.valueOf(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024;
        }
        return Long.valueOf(size);
    }

}
