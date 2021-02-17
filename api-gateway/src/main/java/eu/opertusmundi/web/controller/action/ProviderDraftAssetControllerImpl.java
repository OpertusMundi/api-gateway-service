package eu.opertusmundi.web.controller.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.feign.client.CatalogueFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.FileSystemMessageCode;
import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetResourceCommandDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftSortField;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientSetStatusCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumDraftStatus;
import eu.opertusmundi.common.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.common.model.dto.EnumSortingOrder;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.ProviderAssetService;
import eu.opertusmundi.web.controller.support.CatalogueUtils;
import eu.opertusmundi.web.repository.ProviderRepository;
import eu.opertusmundi.web.validation.AssetResourceValidator;
import eu.opertusmundi.web.validation.DraftValidator;
import feign.FeignException;

@RestController
public class ProviderDraftAssetControllerImpl extends BaseController implements ProviderDraftAssetController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderDraftAssetControllerImpl.class);

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Autowired
    private DraftValidator draftValidator;
   
    @Autowired
    private CatalogueUtils catalogueUtils;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ProviderAssetService providerAssetService;
    
    @Autowired
    private AssetResourceValidator assetResourceValidator;

    @Override
    public RestResponse<?> findAllDraft(
        Set<EnumProviderAssetDraftStatus> status, int pageIndex, int pageSize,
        EnumProviderAssetDraftSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID publisherKey = this.currentUserKey();

            final PageResultDto<AssetDraftDto> result = this.providerAssetService.findAllDraft(
                publisherKey, status, pageIndex, pageSize, orderBy, order
            );

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<AssetDraftDto> createDraft(CatalogueItemCommandDto command, BindingResult validationResult) {
        try {
            command.setPublisherKey(this.currentUserKey());

            command.getPricingModels().stream().forEach(m-> {
                // Always override the key with a value generated at the server
                m.setKey(UUID.randomUUID());
            });

            this.draftValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            final AssetDraftDto result = this.providerAssetService.updateDraft(command);

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<AssetDraftDto> findOneDraft(UUID draftKey) {
        try {
            final UUID publisherKey = this.currentUserKey();

            final AssetDraftDto draft = this.providerAssetService.findOneDraft(publisherKey, draftKey);

            if(draft ==null) {
                return RestResponse.notFound();
            }

            return RestResponse.result(draft);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<AssetDraftDto> updateDraft(UUID draftKey, CatalogueItemCommandDto command, BindingResult validationResult) {
        try {
            command.setPublisherKey(this.currentUserKey());
            command.setAssetKey(draftKey);

            command.getPricingModels().stream().forEach(m-> {
                // Always override the key with a value generated at the server
                m.setKey(UUID.randomUUID());
            });

            this.draftValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            final AssetDraftDto result = this.providerAssetService.updateDraft(command);

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse submitDraft(UUID draftKey, CatalogueItemCommandDto command, BindingResult validationResult) {
        try {
            command.setPublisherKey(this.currentUserKey());
            command.setAssetKey(draftKey);

            this.draftValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
            }

            this.providerAssetService.submitDraft(command);

            return RestResponse.success();
        } catch (final AssetDraftException ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse reviewDraft(UUID draftKey, AssetDraftReviewCommandDto command) {
        try {
            command.setAssetKey(draftKey);
            command.setPublisherKey(this.currentUserKey());

            if (command.isRejected()) {
                this.providerAssetService.rejectProvider(command);
            } else {
                this.providerAssetService.acceptProvider(command);
            }

            return RestResponse.success();
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse deleteDraft(UUID draftKey) {
        try {
            final UUID publisherKey = this.currentUserKey();

            this.providerAssetService.deleteDraft(publisherKey, draftKey);

            return RestResponse.success();
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    public RestResponse<?> findAllDraftTemp(EnumDraftStatus status, int pageIndex, int pageSize) {
        try {
            final UUID publisherKey = this.currentUserKey();

            // Catalogue service data page index is 1-based
            final ResponseEntity<CatalogueResponse<CatalogueCollection>> e = this.catalogueClient.getObject().findAllDraft(
                publisherKey, status.getValue(), pageIndex + 1, pageSize
            );

            // Check if catalogue response is successful
            final CatalogueResponse<CatalogueCollection> catalogueResponse = e.getBody();

            if(!catalogueResponse.isSuccess()) {
                return RestResponse.failure();
            }

            // Process response
            final CatalogueClientCollectionResponse<CatalogueItemDraftDto> response = this.catalogueUtils.createSeachResult(
                catalogueResponse, (item)-> new CatalogueItemDraftDto(item), pageIndex, pageSize
            );

            return response;
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            if (code == BasicMessageCode.NotFound) {
                return RestResponse.result(PageResultDto.<CatalogueItemDto>empty(new PageRequestDto(pageIndex, pageSize)));
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            return RestResponse.failure();
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    public RestResponse<Void> createDraftTemp(CatalogueItemCommandDto command) {
        try {
            // TODO : id must be created by the PID service

            // Inject provider (current user) key
            command.setPublisherKey(this.currentUserKey());

            // Create feature
            final CatalogueFeature feature = command.toFeature();

            command.getPricingModels().stream().forEach(m-> {
                // Always override the key with a value generated at the server
                m.setKey(UUID.randomUUID());
            });

            // Insert new asset
            this.catalogueClient.getObject().createDraft(feature);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    public RestResponse<CatalogueItemDraftDto> findOneDraftTemp(UUID draftKey) {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneDraftById(draftKey);

            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if(!catalogueResponse.isSuccess()) {
                return RestResponse.failure();
            }

            // Convert feature to catalogue item
            final CatalogueItemDraftDto item = new CatalogueItemDraftDto(catalogueResponse.getResult());

            // Inject publisher details
            final PublisherDto publisher = this.providerRepository.findOneByKey(item.getPublisherId()).toPublisherDto();

            item.setPublisher(publisher);

            // Compute effective pricing models
            this.catalogueUtils.refreshPricingModels(item, catalogueResponse.getResult().getProperties().getPricingModels());

            return RestResponse.result(item);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // Convert 404 errors to empty results
            if (code == BasicMessageCode.NotFound) {
                return RestResponse.notFound();
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            return RestResponse.error(code, "An error has occurred");
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    public RestResponse<CatalogueItemDetailsDto> updateDraftTemp(UUID draftKey, CatalogueItemCommandDto command) {
        try {
            final UUID publisherKey = this.currentUserKey();

            // Inject provider and asset identifiers
            command.setPublisherKey(publisherKey);
            command.setAssetKey(draftKey);

            // Create feature
            final CatalogueFeature feature = command.toFeature();

            command.getPricingModels().stream().forEach(m-> {
                // Always override the key with a value generated at the server
                m.setKey(UUID.randomUUID());
            });

            // Update draft
            this.catalogueClient.getObject().updateDraft(draftKey, feature);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    public BaseResponse setDraftStatusTemp(UUID draftKey, CatalogueClientSetStatusCommandDto command) {
        try {
            this.catalogueClient.getObject().setDraftStatus(draftKey.toString(), command.getStatus().getValue());

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    public BaseResponse deleteDraftTemp(UUID draftKey) {
        try {
            this.catalogueClient.getObject().deleteDraft(draftKey);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<?> uploadResource(
        UUID draftKey, MultipartFile resource, AssetResourceCommandDto command, BindingResult validationResult
    ) {
        final UUID publisherKey = this.currentUserKey();

        if (resource == null || resource.getSize() == 0) {
            return RestResponse.error(FileSystemMessageCode.FILE_IS_MISSING, "A file is required");
        }

        command.setDraftKey(draftKey);
        command.setPublisherKey(publisherKey);
        command.setSize(resource.getSize());
        if (StringUtils.isBlank(command.getFileName())) {
            command.setFileName(resource.getOriginalFilename());
        }

        this.assetResourceValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try (final InputStream input = new ByteArrayInputStream(resource.getBytes())) {
            this.providerAssetService.addResource(command, input);
        } catch (final ServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Failed to upload file", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, ex.getMessage());
        }

        final AssetDraftDto draft = this.providerAssetService.findOneDraft(publisherKey, draftKey);

        return RestResponse.result(draft);
    }

    @Override
    public RestResponse<?> uploadAdditionalResource(
        UUID draftKey, MultipartFile resource, AssetFileAdditionalResourceCommandDto command, BindingResult validationResult
    ) {
        final UUID          publisherKey = this.currentUserKey();
       
        if (resource == null || resource.getSize() == 0) {
            return RestResponse.error(FileSystemMessageCode.FILE_IS_MISSING, "A file is required");
        }
               
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try (final InputStream input = new ByteArrayInputStream(resource.getBytes())) {
            command.setDraftKey(draftKey);
            command.setPublisherKey(publisherKey);
            command.setSize(resource.getSize());
            if (StringUtils.isBlank(command.getFileName())) {
                command.setFileName(resource.getOriginalFilename());
            }

            this.providerAssetService.addAdditionalResource(command, input);
        } catch (final ServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Failed to upload file", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, ex.getMessage());
        }

        final AssetDraftDto draft = this.providerAssetService.findOneDraft(publisherKey, draftKey);

        return RestResponse.result(draft);
    }

    public ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        UUID draftKey, UUID resourceKey, HttpServletResponse response
    ) throws IOException {
        final UUID publisherKey = this.currentUserKey();

        final Path path = this.providerAssetService.resolveDraftAdditionalResource(publisherKey, draftKey, resourceKey);
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
    }
    
    public ResponseEntity<StreamingResponseBody> getMetadataProperty(
        UUID draftKey, UUID resourceKey, String propertyName, HttpServletResponse response
    ) throws IOException {
        final UUID publisherKey = this.currentUserKey();

        final MetadataProperty property = this.providerAssetService.resolveDraftMetadataProperty(
            publisherKey, draftKey, resourceKey, propertyName
        );
        
        final File file = property.getPath().toFile();
       
        String contentType = Files.probeContentType(property.getPath());
        if (contentType == null) {
            contentType = property.getType().getMediaType();
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

}
