package eu.opertusmundi.web.controller.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.ValidationMessage;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetContractAnnexCommandDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftSortField;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.ExternalUrlFileResourceCommandDto;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.asset.UserFileResourceCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemMetadataCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftFromAssetCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.contract.provider.ProviderUploadContractCommand;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.ProviderAssetService;
import eu.opertusmundi.common.service.UserFileManager;
import eu.opertusmundi.web.validation.ApiDraftValidator;
import eu.opertusmundi.web.validation.AssetFileResourceValidator;
import eu.opertusmundi.web.validation.DraftFromAssetValidator;
import eu.opertusmundi.web.validation.DraftReviewValidator;
import eu.opertusmundi.web.validation.DraftValidator;
import eu.opertusmundi.web.validation.DraftValidator.EnumValidationMode;

@RestController
public class ProviderDraftAssetControllerImpl extends BaseController implements ProviderDraftAssetController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderDraftAssetControllerImpl.class);

    private final List<String> allowedMimeTypes = Arrays.asList(new String[]{
        MediaType.APPLICATION_PDF_VALUE,
    });

    @Autowired
    private DraftValidator draftValidator;

    @Autowired
    private UserFileManager userFileManager;

    @Autowired
    private DraftFromAssetValidator draftFromAssetValidator;

    @Autowired
    private DraftReviewValidator draftReviewValidator;

    @Autowired
    private ApiDraftValidator apiDraftValidator;

    @Autowired
    private AssetFileResourceValidator assetResourceValidator;

    @Autowired
    private ProviderAssetService providerAssetService;

    @Override
    public RestResponse<?> findAllDraft(
        Set<EnumProviderAssetDraftStatus> status, Set<EnumAssetType> type, Set<EnumSpatialDataServiceType> serviceType,
        int pageIndex, int pageSize,
        EnumProviderAssetDraftSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID ownerKey     = this.currentUserKey();
            final UUID publisherKey = this.currentUserParentKey();

            final PageResultDto<AssetDraftDto> result = this.providerAssetService.findAllDraft(
                ownerKey, publisherKey, status, type, serviceType, pageIndex, pageSize, orderBy, order
            );

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<AssetDraftDto> createDraft(boolean lock, CatalogueItemCommandDto command, BindingResult validationResult) {
        // Inject command properties
        this.injectCatalogueItemCommandProperties(command, lock);
        // Authorize command
        this.authorizeCommand(command);

        try {
            this.draftValidator.validate(command, validationResult, EnumValidationMode.UPDATE);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            final AssetDraftDto result = this.providerAssetService.updateDraft(command);

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<AssetDraftDto> createDraftFromAsset(
        boolean lock, DraftFromAssetCommandDto command, BindingResult validationResult
    ) {
        try {
            command.setPublisherKey(this.currentUserParentKey());
            command.setOwnerKey(this.currentUserKey());
            command.setLocked(lock);

            this.draftFromAssetValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            final AssetDraftDto result = this.providerAssetService.createDraftFromAsset(command);

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }
        return RestResponse.failure();
    }

    @Override
    public BaseResponse createApiDraft(boolean lock, DraftApiCommandDto command, BindingResult validationResult) {
        try {
            command.setPublisherKey(this.currentUserParentKey());
            command.setOwnerKey(this.currentUserKey());
            command.setUserName(this.currentUserEmail());
            command.setLocked(lock);

            this.apiDraftValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            final AssetDraftDto result = this.providerAssetService.createApiDraft(command);

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<AssetDraftDto> findOneDraft(UUID draftKey, boolean lock) {
        try {
            final UUID ownerKey     = this.currentUserKey();
            final UUID publisherKey = this.currentUserParentKey();

            final AssetDraftDto draft = this.providerAssetService.findOneDraft(ownerKey, publisherKey, draftKey, lock);

            if (draft == null) {
                return RestResponse.notFound();
            }

            return RestResponse.result(draft);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<AssetDraftDto> updateDraft(
        UUID draftKey, boolean lock, CatalogueItemCommandDto command, BindingResult validationResult
    ) {
        // Inject command properties
        this.injectCatalogueItemCommandProperties(draftKey, command, lock);
        // Authorize command
        this.authorizeCommand(command);

        try {
            this.draftValidator.validate(command, validationResult, EnumValidationMode.UPDATE, draftKey);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            final AssetDraftDto result = this.providerAssetService.updateDraft(command);

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse submitDraft(UUID draftKey, CatalogueItemCommandDto command, BindingResult validationResult) {
        // Inject command properties
        this.injectCatalogueItemCommandProperties(draftKey, command, false);
        // Authorize command
        this.authorizeCommand(command);

        try {
            this.draftValidator.validate(command, validationResult, EnumValidationMode.SUBMIT, draftKey);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            this.providerAssetService.submitDraft(command);

            return RestResponse.success();
        } catch (final AssetDraftException ex) {
            logger.error("Operation has failed", ex);

            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse reviewDraft(UUID draftKey, AssetDraftReviewCommandDto command) {
        try {
            final UUID ownerKey     = this.currentUserKey();
            final UUID publisherKey = this.currentUserParentKey();

            command.setDraftKey(draftKey);
            command.setOwnerKey(ownerKey);
            command.setPublisherKey(publisherKey);

            if (command.isRejected()) {
                this.providerAssetService.rejectProvider(command);
            } else {
                this.providerAssetService.acceptProvider(command);
            }

            return RestResponse.success();
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse deleteDraft(UUID draftKey) {
        try {
            final UUID ownerKey     = this.currentUserKey();
            final UUID publisherKey = this.currentUserParentKey();

            this.providerAssetService.deleteDraft(ownerKey, publisherKey, draftKey);

            return RestResponse.success();
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<?> addResourceFromFileSystem(
        UUID draftKey, UserFileResourceCommandDto command, BindingResult validationResult
    ) {
        final String userName     = this.currentUserEmail();
        final UUID   ownerKey     = this.currentUserKey();
        final UUID   publisherKey = this.currentUserParentKey();

        command.setDraftKey(draftKey);
        command.setOwnerKey(ownerKey);
        command.setPublisherKey(publisherKey);
        command.setUserName(userName);

        // Resolve file
        final FilePathCommand fileCommand = FilePathCommand.builder()
            .path(command.getPath())
            .userName(command.getUserName())
            .build();

        try {
            final Path path = this.userFileManager.resolveFilePath(fileCommand);

            command.setSize(Files.size(path));
            if (StringUtils.isBlank(command.getFileName())) {
                command.setFileName(path.getFileName().toString());
            }
        } catch (final IOException | FileSystemException ex) {
            validationResult.rejectValue("path", EnumValidatorError.FileNotFound.name());
        }

        this.assetResourceValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
        }

        this.providerAssetService.addFileResourceFromFileSystem(command);

        final AssetDraftDto draft = this.providerAssetService.findOneDraft(ownerKey, publisherKey, draftKey, false);

        return RestResponse.result(draft);
    }

    @Override
    public RestResponse<?> addResourceFromExternalUrl(
        UUID draftKey, ExternalUrlFileResourceCommandDto command, BindingResult validationResult
    ) {
        final UUID   ownerKey     = this.currentUserKey();
        final UUID   publisherKey = this.currentUserParentKey();

        command.setDraftKey(draftKey);
        command.setOwnerKey(ownerKey);
        command.setPublisherKey(publisherKey);

        this.assetResourceValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
        }

        this.providerAssetService.addResource(publisherKey, draftKey, command.toResource());

        final AssetDraftDto draft = this.providerAssetService.findOneDraft(ownerKey, publisherKey, draftKey, false);

        return RestResponse.result(draft);
    }

    @Override
    public RestResponse<?> uploadResource(
        UUID draftKey, MultipartFile resource, FileResourceCommandDto command, BindingResult validationResult
    ) {
        final UUID ownerKey     = this.currentUserKey();
        final UUID publisherKey = this.currentUserParentKey();

        if (resource == null || resource.getSize() == 0) {
            return RestResponse.error(FileSystemMessageCode.FILE_IS_MISSING, "A file is required");
        }

        command.setDraftKey(draftKey);
        command.setOwnerKey(ownerKey);
        command.setPublisherKey(publisherKey);
        command.setSize(resource.getSize());
        if (StringUtils.isBlank(command.getFileName())) {
            command.setFileName(resource.getOriginalFilename());
        }

        this.assetResourceValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
        }

        try (final InputStream input = new ByteArrayInputStream(resource.getBytes())) {
            this.providerAssetService.addFileResourceFromUpload(command, input);
        } catch (final ServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error(String.format("Failed to upload file. [publisherKey=%s, draftKey=%s]", publisherKey, draftKey), ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, ex.getMessage());
        }

        final AssetDraftDto draft = this.providerAssetService.findOneDraft(ownerKey, publisherKey, draftKey, false);

        return RestResponse.result(draft);
    }

    @Override
    public RestResponse<?> uploadAdditionalResource(
        UUID draftKey, MultipartFile resource, AssetAdditionalResourceCommandDto command, BindingResult validationResult
    ) {
        final UUID ownerKey     = this.currentUserKey();
        final UUID publisherKey = this.currentUserParentKey();

        if (resource == null || resource.getSize() == 0) {
            return RestResponse.error(FileSystemMessageCode.FILE_IS_MISSING, "A file is required");
        }

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try (final InputStream input = new ByteArrayInputStream(resource.getBytes())) {
            command.setDraftKey(draftKey);
            command.setOwnerKey(ownerKey);
            command.setPublisherKey(publisherKey);
            command.setSize(resource.getSize());
            if (StringUtils.isBlank(command.getFileName())) {
                command.setFileName(resource.getOriginalFilename());
            }

            this.providerAssetService.addAdditionalResource(command, input);
        } catch (final ServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error(String.format("Failed to upload file. [publisherKey=%s, draftKey=%s]", publisherKey, draftKey), ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, ex.getMessage());
        }

        final AssetDraftDto draft = this.providerAssetService.findOneDraft(ownerKey, publisherKey, draftKey, false);

        return RestResponse.result(draft);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        UUID draftKey, String resourceKey, HttpServletResponse response
    ) throws IOException {
        final UUID ownerKey     = this.currentUserKey();
        final UUID publisherKey = this.currentUserParentKey();

        final Path path = this.providerAssetService.resolveDraftAdditionalResource(ownerKey, publisherKey, draftKey, resourceKey);
        final File file = path.toFile();

        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
        response.setHeader("Content-Type", contentType);
        if (file.length() < 1024 * 1024) {
            response.setHeader("Content-Length", Long.toString(file.length()));
        }

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }

    @Override
    public RestResponse<?> uploadContract(UUID draftKey, boolean lock, MultipartFile file) {
        final UUID ownerKey     = this.currentUserKey();
        final UUID publisherKey = this.currentUserParentKey();

        if (file == null || file.getSize() == 0) {
            return RestResponse.error(FileSystemMessageCode.FILE_IS_MISSING, "A file is required");
        }

        try (final InputStream input = new ByteArrayInputStream(file.getBytes())) {
            final byte[] data     = IOUtils.toByteArray(input);
            final String mimeType = this.detectMimeType(data);

            if (mimeType == null) {
                final ValidationMessage message = new ValidationMessage("file", "FileTypeNotSupported", null, null);
                return RestResponse.error(message);
            }

            final ProviderUploadContractCommand command = ProviderUploadContractCommand.builder()
                .draftKey(draftKey)
                .ownerKey(ownerKey)
                .locked(lock)
                .publisherKey(publisherKey)
                .build();

            command.setSize(file.getSize());
            if (StringUtils.isBlank(command.getFileName())) {
                command.setFileName(file.getOriginalFilename());
            }

            this.providerAssetService.setContract(command, data);
        } catch (final ServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error(String.format("Failed to upload file. [ draftKey=%s]", draftKey), ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, ex.getMessage());
        }

        final AssetDraftDto draft = this.providerAssetService.findOneDraft(ownerKey, publisherKey, draftKey, false);

        return RestResponse.result(draft);
    }

    @Override
    public RestResponse<?> uploadContractAnnex(
        UUID draftKey, MultipartFile file, AssetContractAnnexCommandDto command, BindingResult validationResult
    ) {
        final UUID ownerKey     = this.currentUserKey();
        final UUID publisherKey = this.currentUserParentKey();

        if (file == null || file.getSize() == 0) {
            return RestResponse.error(FileSystemMessageCode.FILE_IS_MISSING, "A file is required");
        }

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try (final InputStream input = new ByteArrayInputStream(file.getBytes())) {
            final byte[] data     = IOUtils.toByteArray(input);
            final String mimeType = this.detectMimeType(data);

            if (mimeType == null) {
                final ValidationMessage message = new ValidationMessage("file", "FileTypeNotSupported", null, null);
                return RestResponse.error(message);
            }

            command.setDraftKey(draftKey);
            command.setOwnerKey(ownerKey);
            command.setPublisherKey(publisherKey);
            command.setSize(file.getSize());
            if (StringUtils.isBlank(command.getFileName())) {
                command.setFileName(file.getOriginalFilename());
            }

            this.providerAssetService.addContractAnnex(command, data);
        } catch (final ServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error(String.format("Failed to upload file. [publisherKey=%s, draftKey=%s]", publisherKey, draftKey), ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, ex.getMessage());
        }

        final AssetDraftDto draft = this.providerAssetService.findOneDraft(ownerKey, publisherKey, draftKey, false);

        return RestResponse.result(draft);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadContract(
        UUID draftKey, HttpServletResponse response
    ) throws IOException {
        final UUID ownerKey     = this.currentUserKey();
        final UUID publisherKey = this.currentUserParentKey();

        final Path path = this.providerAssetService.resolveDraftContractPath(ownerKey, publisherKey, draftKey);
        final File file = path.toFile();

        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
        response.setHeader("Content-Type", contentType);
        if (file.length() < 1024 * 1024) {
            response.setHeader("Content-Length", Long.toString(file.length()));
        }

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadContractAnnex(
        UUID draftKey, String annexKey, HttpServletResponse response
    ) throws IOException {
        final UUID ownerKey     = this.currentUserKey();
        final UUID publisherKey = this.currentUserParentKey();

        final Path path = this.providerAssetService.resolveDraftContractAnnexPath(ownerKey, publisherKey, draftKey, annexKey);
        final File file = path.toFile();

        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
        response.setHeader("Content-Type", contentType);
        if (file.length() < 1024 * 1024) {
            response.setHeader("Content-Length", Long.toString(file.length()));
        }

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getMetadataProperty(
        UUID draftKey, String resourceKey, String propertyName, HttpServletResponse response
    ) throws IOException {
        final UUID ownerKey     = this.currentUserKey();
        final UUID publisherKey = this.currentUserParentKey();

        final MetadataProperty property = this.providerAssetService.resolveDraftMetadataProperty(
            ownerKey, publisherKey, draftKey, resourceKey, propertyName
        );

        final File file = property.getPath().toFile();

        String contentType = Files.probeContentType(property.getPath());
        if (contentType == null) {
            contentType = property.getType().getMediaType();
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
        response.setHeader("Content-Type", contentType);
        if (file.length() < 1024 * 1024) {
            response.setHeader("Content-Length", Long.toString(file.length()));
        }

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }

    @Override
    public RestResponse<AssetDraftDto> updateDraftMetadata(
        UUID draftKey, CatalogueItemMetadataCommandDto command, BindingResult validationResult
    ) {
        try {
            final UUID ownerKey     = this.currentUserKey();
            final UUID publisherKey = this.currentUserParentKey();

            command.setDraftKey(draftKey);
            command.setOwnerKey(ownerKey);
            command.setPublisherKey(publisherKey);

            this.draftReviewValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            final AssetDraftDto result = this.providerAssetService.updateDraftMetadata(command);

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse releaseLock(UUID draftKey) {
        try {
            this.providerAssetService.releaseLock(this.currentUserKey(), draftKey);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        }

        return RestResponse.success();
    }

    private void authorizeCommand(@Nullable CatalogueItemCommandDto command) {
        if (command == null) {
            return;
        }
        final EnumAssetType type = command.getType();

        if (type == null) {
            return;
        }
        final EnumRole requiredRole = type.getRequiredRole();

        if (requiredRole != null && !this.hasRole(requiredRole)) {
            throw new AccessDeniedException("Access Denied");
        }

        if (command.isOpenDataset() && !this.hasAnyRole(EnumRole.ROLE_PROVIDER_OPEN_DATASET)) {
            throw new AccessDeniedException("Access Denied");
        }
    }

    private void injectCatalogueItemCommandProperties(CatalogueItemCommandDto command, boolean lock) {
        this.injectCatalogueItemCommandProperties(null, command, lock);
    }

    private void injectCatalogueItemCommandProperties(UUID draftKey, CatalogueItemCommandDto command, boolean lock) {
        command.setDraftKey(draftKey);
        command.setLocked(lock);
        command.setOwnerKey(this.currentUserKey());
        command.setPublisherKey(this.currentUserParentKey());

        command.getPricingModels().stream().forEach(m-> {
            // Always override the key with a value generated at the server
            m.setKey(UUID.randomUUID().toString());
        });
    }

    private String detectMimeType(byte[] data) {
        final Tika tika = new Tika();
        final String mimeType  = tika.detect(data);

        if(!allowedMimeTypes.contains(mimeType)) {
            return null;
        }

        return mimeType;
    }

}
