package eu.opertusmundi.web.controller.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.repository.ProviderRepository;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.ProviderAssetService;
import eu.opertusmundi.common.util.CatalogueItemUtils;

@RestController
public class HelpdeskDraftAssetControllerImpl extends BaseController implements HelpdeskDraftAssetController {

    private static final Logger logger = LoggerFactory.getLogger(HelpdeskDraftAssetControllerImpl.class);

    private final ProviderRepository providerRepository;

    private final ProviderAssetService providerAssetService;

    private final CatalogueItemUtils catalogueItemUtils;

    @Autowired
    public HelpdeskDraftAssetControllerImpl(
        ProviderRepository providerRepository,
        ProviderAssetService providerAssetService,
        CatalogueItemUtils catalogueItemUtils
    ) {
        this.providerRepository   = providerRepository;
        this.providerAssetService = providerAssetService;
        this.catalogueItemUtils   = catalogueItemUtils;
    }

    @Override
    public RestResponse<CatalogueItemDetailsDto> findOneDraft(UUID draftKey) {
        try {
            final AssetDraftDto draft = this.providerAssetService.findOneDraft(draftKey);

            if (draft == null || draft.getStatus() != EnumProviderAssetDraftStatus.PENDING_HELPDESK_REVIEW) {
                return RestResponse.notFound();
            }

            final CatalogueFeature        feature = draft.getCommand().toFeature();
            final CatalogueItemDetailsDto item    = new CatalogueItemDetailsDto(feature);

            // Inject publisher details
            final ProviderDto publisher = this.providerRepository.findOneByKey(draft.getPublisher().getKey()).getProvider().toProviderDto(true);
            item.setPublisherId(publisher.getKey());
            item.setPublisher(publisher);

            // Inject contract details
            this.catalogueItemUtils.setContract(item, draft);

            // Update metadata property URLs
            this.providerAssetService.updateMetadataPropertyLinks(
                draft.getKey().toString(), item.getResources(), item.getAutomatedMetadata(), draft.getStatus()
            );

            // Compute effective pricing models
            catalogueItemUtils.refreshPricingModels(item);

            return RestResponse.result(item);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        UUID draftKey, String resourceKey, HttpServletResponse response
    ) throws IOException {
        final AssetDraftDto draft        = this.providerAssetService.findOneDraft(draftKey);
        final UUID          publisherKey = draft.getPublisher().getKey();
        // We set publisher key to the owner key value. Helpdesk account can
        // review any draft
        final Path          path         = this.providerAssetService.resolveDraftAdditionalResource(publisherKey, publisherKey, draftKey, resourceKey);
        final File          file         = path.toFile();

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

    @Override
    public ResponseEntity<StreamingResponseBody> getMetadataProperty(
        UUID draftKey, String resourceKey, String propertyName, HttpServletResponse response
    ) throws IOException {
        final AssetDraftDto    draft        = this.providerAssetService.findOneDraft(draftKey);
        final UUID             publisherKey = draft.getPublisher().getKey();
        // We set publisher key to the owner key value. Helpdesk account can
        // review any draft
        final MetadataProperty property     = this.providerAssetService.resolveDraftMetadataProperty(
            publisherKey, publisherKey, draftKey, resourceKey, propertyName
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
