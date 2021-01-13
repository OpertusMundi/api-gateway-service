package eu.opertusmundi.web.controller.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.file.DirectoryDto;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileUploadCommand;
import eu.opertusmundi.common.service.UserFileManager;

@RestController
public class FileSystemControllerImpl extends BaseController implements FileSystemController {

    @Autowired
    private UserFileManager fileManager;

    @Override
    public RestResponse<?> browseDirectory() {
        this.ensureRegistered();

        return this.browse();
    }

    @Override
    public RestResponse<?> createFolder(FilePathCommand command) {
        this.ensureRegistered();

        try {
            command.setUserId(this.currentUserId());

            this.fileManager.createPath(command);

            return this.browse();
        } catch (final FileSystemException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadFile(String relativePath, HttpServletResponse response) throws IOException {
        this.ensureRegistered();

        try {
            final FilePathCommand command = FilePathCommand.builder()
                .userId(this.currentUserId())
                .path(relativePath)
                .build();

            final Path path = this.fileManager.resolveFilePath(command);
            final File file = path.toFile();

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
            response.setHeader("Content-Type", contentType);
            response.setHeader("Content-Length", Long.toString(file.length()));

            final StreamingResponseBody stream = out -> {
                try (InputStream inputStream = new FileInputStream(file)) {
                    IOUtils.copyLarge(inputStream, out);
                }
            };

            return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
        } catch (final FileSystemException ex) {
            throw ex;
        }
    }

    @Override
    public RestResponse<?> deletePath(String path) {
        this.ensureRegistered();

        try {
            final FilePathCommand command = FilePathCommand.builder()
                .userId(this.currentUserId())
                .path(path)
                .build();

            this.fileManager.deletePath(command);

            return this.browse();
        } catch (final FileSystemException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    @Override
    public RestResponse<?> uploadFile(MultipartFile file, FileUploadCommand command) throws IOException {
        this.ensureRegistered();

        try (final InputStream input = new ByteArrayInputStream(file.getBytes())) {
            command.setUserId(this.currentUserId());
            command.setSize(file.getSize());

            this.fileManager.uploadFile(input, command);

            return this.browse();
        } catch (final FileSystemException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

    private RestResponse<?> browse() {
        try {
            final FilePathCommand command = FilePathCommand.builder()
                .userId(this.currentUserId())
                .path("/")
                .build();

            final DirectoryDto result = this.fileManager.browse(command);

            return RestResponse.result(result);
        } catch (final FileSystemException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }
    }

}