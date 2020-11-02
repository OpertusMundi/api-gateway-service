package eu.opertusmundi.web.controller.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumOwningEntityType;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.filemanager.FileDeleteCommand;
import eu.opertusmundi.web.model.filemanager.FileDownloadCommand;
import eu.opertusmundi.web.model.filemanager.FileUploadCommand;
import eu.opertusmundi.web.service.FileManager;
import eu.opertusmundi.web.service.FileManagerResolver;

@RestController
public class FileManagerControllerImpl extends BaseController implements FileManagerController {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerControllerImpl.class);

    @Autowired
    private FileManagerResolver resolver;

    @Value("${opertusmundi.file-system.tmp-dir}")
    private String temporaryDir;

    @Override
    public ResponseEntity<StreamingResponseBody> downloadFile(
            EnumOwningEntityType type, UUID ownerKey, UUID fileKey, HttpServletResponse response
    ) throws IOException {
        try {
            final FileDownloadCommand command = new FileDownloadCommand();

            command.setFileKey(fileKey);
            command.setOwningEntityKey(ownerKey);
            command.setOwningEntityType(type);
            command.setUserKey(this.currentUserKey());

            // Resolve file manager and create file
            final FileManager fm   = this.resolver.getFileManager(type);
            final Path        path = fm.getFileAbsolutePath(command);
            final File        file = path.toFile();

            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File does not exist");
            } else {
                logger.info("User {} ({}) has downloaded file {}", this.currentUserEmail(), this.currentUserId(), path.toString());

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
            }
        } catch (final Exception ex) {
            logger.warn("Failed to download file [{}] for user {} ({})", fileKey, this.currentUserEmail(), this.currentUserId());

            response.sendError(HttpServletResponse.SC_GONE, "File has been removed");
        }
        return null;
    }

    @Override
    public RestResponse<?> deleteFile(EnumOwningEntityType type, UUID key, UUID file) {
        try {
            final FileDeleteCommand command = new FileDeleteCommand();

            command.setFileKey(file);
            command.setOwningEntityKey(key);
            command.setOwningEntityType(type);
            command.setUserKey(this.currentUserKey());

            // Resolve file manager and create file
            final FileManager fm = this.resolver.getFileManager(type);
            fm.deleteFile(command);

            return RestResponse.success();
        } catch (final IOException ex) {
            return RestResponse.error(BasicMessageCode.IOError, "Failed to create file");
        } catch (final Exception ex) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    @Override
    public RestResponse<?> uploadFile(
            EnumOwningEntityType type, UUID key, MultipartFile file, FileUploadCommand command
    ) {
        try {
            // Create temporary copy of the uploaded file
            final Path temporaryPath = Paths.get(this.temporaryDir, UUID.randomUUID().toString());

            try (final InputStream in = new ByteArrayInputStream(file.getBytes())) {
                Files.copy(in, temporaryPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Inject additional context in the command
            command.setOwningEntityKey(key);
            command.setOwningEntityType(type);
            command.setUserKey(this.currentUserKey());
            command.setPath(temporaryPath);
            if (StringUtils.isBlank(command.getFilename())) {
                // TODO: Handle case when a path is sent by the browser
                command.setFilename(file.getOriginalFilename());
            }

            // Resolve file manager and create file
            final FileManager fm = this.resolver.getFileManager(type);
            fm.uploadFile(command);

            return RestResponse.success();
        } catch (final IOException ex) {
            return RestResponse.error(BasicMessageCode.IOError, "Failed to create file");
        } catch (final Exception ex) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

}
