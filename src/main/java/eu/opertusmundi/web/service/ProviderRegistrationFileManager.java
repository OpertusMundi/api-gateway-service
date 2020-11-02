package eu.opertusmundi.web.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.EnumOwningEntityType;
import eu.opertusmundi.common.model.EnumProviderRegistrationFileStatus;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.domain.FileUploadEntity;
import eu.opertusmundi.web.domain.ProviderRegistrationEntity;
import eu.opertusmundi.web.domain.ProviderRegistrationFileEntity;
import eu.opertusmundi.web.model.filemanager.FileDeleteCommand;
import eu.opertusmundi.web.model.filemanager.FileDownloadCommand;
import eu.opertusmundi.web.model.filemanager.FileUploadCommand;
import eu.opertusmundi.web.repository.AccountRepository;
import eu.opertusmundi.web.repository.FileUploadRepository;

@FileManagerType(EnumOwningEntityType.PROVIDER_REGISTRATION)
@Service
public class ProviderRegistrationFileManager implements FileManager {

    @Value("${opertusmundi.file-system.data-dir}")
    private String dataDir;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Override
    @Transactional
    public void uploadFile(FileUploadCommand command) throws IOException {
        final ZonedDateTime now = ZonedDateTime.now();

        // The owning entity is the account
        if (!command.getOwningEntityKey().equals(command.getUserKey())) {
            throw new EntityNotFoundException();
        }

        // Get account
        final AccountEntity account = this.accountRepository.findOneByKey(command.getUserKey()).orElse(null);

        if (account == null) {
            throw new EntityNotFoundException();
        }

        // Check if an active registration request exists
        final ProviderRegistrationEntity registration = account.getProfile().getProviderRegistration();

        if (registration == null) {
            throw new EntityNotFoundException();
        }

        // Create upload
        final FileUploadEntity upload = new FileUploadEntity(command.getUserKey());

        upload.setAccount(account);
        upload.setComment(command.getComment());
        upload.setCreatedOn(now);
        upload.setFileName(command.getFilename());
        upload.setOwningEntityType(EnumOwningEntityType.PROVIDER_REGISTRATION);
        upload.setSize(Files.size(command.getPath()));

        final String relativePath = this.resolveFileRelativePath(upload);
        upload.setRelativePath(relativePath);

        this.fileUploadRepository.saveAndFlush(upload);

        // Create file
        final ProviderRegistrationFileEntity file = new ProviderRegistrationFileEntity();

        file.setCreatedOn(now);
        file.setFile(upload);
        file.setModifiedBy(account);
        file.setModifiedOn(now);
        file.setRegistration(registration);
        file.setStatus(EnumProviderRegistrationFileStatus.PENDING);

        registration.getFiles().add(file);

        // Copy file
        FileUtils.copyFile(command.getPath().toFile(), Paths.get(this.dataDir, relativePath).toFile());
        FileUtils.deleteQuietly(command.getPath().toFile());

        this.accountRepository.saveAndFlush(account);
    }

    @Override
    @Transactional
    public void deleteFile(FileDeleteCommand command) {
        // The owning entity is the account
        if (!command.getOwningEntityKey().equals(command.getUserKey())) {
            throw new EntityNotFoundException();
        }

        // Get account
        final AccountEntity account = this.accountRepository.findOneByKey(command.getUserKey()).orElse(null);

        if (account == null) {
            throw new EntityNotFoundException();
        }

        // Check if an active registration request exists
        final ProviderRegistrationEntity registration = account.getProfile().getProviderRegistration();

        if (registration == null) {
            throw new EntityNotFoundException();
        }

        // Get registration file
        final ProviderRegistrationFileEntity file = registration.getFiles().stream()
            .filter(f -> f.getFile().getKey().equals(command.getFileKey()))
            .findFirst()
            .orElse(null);

        if(file == null) {
            throw new EntityNotFoundException();
        }

        // Get uploaded file
        final FileUploadEntity upload = file.getFile();

        // Delete registration file first
        registration.getFiles().remove(file);
        this.accountRepository.saveAndFlush(account);

        // Delete upload file
        this.fileUploadRepository.delete(upload);
        this.fileUploadRepository.flush();

        // Database must be updated before deleting the actual file

        // Delete file
        final Path targetFilePath =  Paths.get(this.dataDir, file.getFile().getRelativePath());
        FileUtils.deleteQuietly(targetFilePath.toFile());
    }

    @Override
    @Transactional(readOnly = true)
    public Path getFileAbsolutePath(FileDownloadCommand command) {
        // The owning entity is the account
        if (!command.getOwningEntityKey().equals(command.getUserKey())) {
            throw new EntityNotFoundException();
        }

        // Get account
        final AccountEntity account = this.accountRepository.findOneByKey(command.getUserKey()).orElse(null);

        if (account == null) {
            throw new EntityNotFoundException();
        }

        // Check if an active registration request exists
        final ProviderRegistrationEntity registration = account.getProfile().getProviderRegistration();

        if (registration == null) {
            throw new EntityNotFoundException();
        }

        // Get registration file
        final ProviderRegistrationFileEntity file = registration.getFiles().stream()
            .filter(f -> f.getFile().getKey().equals(command.getFileKey()))
            .findFirst()
            .orElse(null);

        if(file == null) {
            throw new EntityNotFoundException();
        }

        return Paths.get(this.dataDir, file.getFile().getRelativePath());
    }

    private String resolveFileRelativePath(FileUploadEntity upload) {
        return Paths.get(
            upload.getOwningEntityType().toString(),
            upload.getOwningEntityKey().toString(),
            upload.getKey().toString() + "_" + upload.getFileName()
        ).toString();
    }
}
