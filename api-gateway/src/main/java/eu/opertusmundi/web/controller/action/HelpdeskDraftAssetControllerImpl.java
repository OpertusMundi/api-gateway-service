package eu.opertusmundi.web.controller.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.contract.ContractDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.repository.ProviderRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.ProviderAssetService;
import eu.opertusmundi.common.service.QuotationService;

@RestController
public class HelpdeskDraftAssetControllerImpl extends BaseController implements HelpdeskDraftAssetController {

    private static final Logger logger = LoggerFactory.getLogger(HelpdeskDraftAssetControllerImpl.class);

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ProviderTemplateContractHistoryRepository contractRepository;

    @Autowired
    private ProviderAssetService providerAssetService;

    @Autowired
    private QuotationService quotationService;

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
            final ProviderDto publisher = this.providerRepository.findOneByKey(draft.getPublisher().getKey()).getProvider().toProviderDto();
            item.setPublisherId(publisher.getKey());
            item.setPublisher(publisher);

            // Inject contract details
            final ContractDto contract = this.contractRepository.findByKey(
                draft.getPublisher().getKey(),
                draft.getCommand().getContractTemplateKey()
            ).map(ProviderTemplateContractHistoryEntity::toSimpleDto).orElse(null);
            item.setContractTemplateId(contract.getId());
            item.setContractTemplateVersion(contract.getVersion());
            item.setContract(contract);

            // Update metadata property URLs
            this.providerAssetService.updateMetadataPropertyLinks(
                draft.getKey().toString(), item.getResources(), item.getAutomatedMetadata(), draft.getStatus()
            );

            // Compute effective pricing models
            this.refreshPricingModels(item);

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
        UUID draftKey, UUID resourceKey, HttpServletResponse response
    ) throws IOException {
        final AssetDraftDto draft        = this.providerAssetService.findOneDraft(draftKey);
        final UUID          publisherKey = draft.getPublisher().getKey();
        final Path          path         = this.providerAssetService.resolveDraftAdditionalResource(publisherKey, draftKey, resourceKey);
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
        UUID draftKey, UUID resourceKey, String propertyName, HttpServletResponse response
    ) throws IOException {
        final AssetDraftDto    draft        = this.providerAssetService.findOneDraft(draftKey);
        final UUID             publisherKey = draft.getPublisher().getKey();
        final MetadataProperty property     = this.providerAssetService.resolveDraftMetadataProperty(
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

    /**
     * Compute pricing models effective values for a catalogue item
     *
     * @param item
     */
    private void refreshPricingModels(CatalogueItemDto item) {
        final List<BasePricingModelCommandDto> models = item.getPricingModels();

        if (models.isEmpty()) {
            return;
        }

        final List<EffectivePricingModelDto> quotations = quotationService.createQuotation(item);

        item.setEffectivePricingModels(quotations);
    }

}
