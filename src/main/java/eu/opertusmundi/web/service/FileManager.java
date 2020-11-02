package eu.opertusmundi.web.service;

import java.io.IOException;
import java.nio.file.Path;

import eu.opertusmundi.web.model.filemanager.FileDeleteCommand;
import eu.opertusmundi.web.model.filemanager.FileDownloadCommand;
import eu.opertusmundi.web.model.filemanager.FileUploadCommand;

public interface FileManager {

    void uploadFile(FileUploadCommand command) throws IOException;

    void deleteFile(FileDeleteCommand command) throws IOException;

    Path getFileAbsolutePath(FileDownloadCommand command);

}
