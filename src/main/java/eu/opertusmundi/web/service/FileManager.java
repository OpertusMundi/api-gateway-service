package eu.opertusmundi.web.service;

import java.io.InputStream;
import java.nio.file.Path;

import eu.opertusmundi.web.model.file.DirectoryDto;
import eu.opertusmundi.web.model.file.FilePathCommand;
import eu.opertusmundi.web.model.file.FileSystemException;
import eu.opertusmundi.web.model.file.FileUploadCommand;

public interface FileManager {

    DirectoryDto browse(FilePathCommand command) throws FileSystemException;

    void createPath(FilePathCommand command) throws FileSystemException;

    void uploadFile(InputStream input, FileUploadCommand command) throws FileSystemException;

    void deletePath(FilePathCommand command) throws FileSystemException;

    Path resolveFilePath(FilePathCommand command) throws FileSystemException;

}
