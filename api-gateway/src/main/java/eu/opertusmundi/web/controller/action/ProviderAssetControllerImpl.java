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

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.EnumProviderAssetSortField;
import eu.opertusmundi.common.model.asset.EnumProviderSubSortField;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceMessageCode;
import eu.opertusmundi.common.model.catalogue.client.CatalogueAssetQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.UnpublishAssetCommand;
import eu.opertusmundi.common.model.payment.provider.ProviderAccountSubscriptionDto;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.ProviderAssetService;

@RestController
public class ProviderAssetControllerImpl extends BaseController implements ProviderAssetController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderAssetController.class);

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private ProviderAssetService providerAssetService;

    @Override
    public RestResponse<?> findAllAssets(
        String query, EnumAssetType type, int pageIndex, int pageSize, EnumProviderAssetSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID                 publisherKey = this.currentUserKey();
            final CatalogueAssetQuery searchQuery  = CatalogueAssetQuery.builder()
                .page(pageIndex)
                .size(pageSize)
                .publisherKey(publisherKey.toString())
                .query(query)
                .build();

            final CatalogueResult<CatalogueItemDto> result = this.catalogueService.findAll(this.createContext(true), searchQuery);

            return CatalogueClientCollectionResponse.of(result.getResult(), result.getPublishers());
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> findAllSubscriptions(
        int pageIndex, int pageSize, EnumProviderSubSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID                                          publisherKey = this.currentUserKey();
            final PageResultDto<ProviderAccountSubscriptionDto> result       = this.providerAssetService.findAllSubscriptions(
                publisherKey, pageIndex, pageSize, orderBy, order
            );

            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getAdditionalResourceFile(
        String pid, String resourceKey, HttpServletResponse response
    ) throws IOException {
        final Path path = this.providerAssetService.resolveAssetAdditionalResource(pid, resourceKey);
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
    
    @Override
    public ResponseEntity<StreamingResponseBody> getContractFile(
        String pid, HttpServletResponse response
    ) throws IOException {
        final Path path = this.providerAssetService.resolveAssetUploadedContractPath(pid);
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
    
    @Override
    public ResponseEntity<StreamingResponseBody> getContractAnnexFile(
        String pid, String annexKey, HttpServletResponse response
    ) throws IOException {
        final Path path = this.providerAssetService.resolveAssetContractAnnex(pid, annexKey);
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

    @Override
    public ResponseEntity<StreamingResponseBody> getMetadataProperty(
        String pid, String resourceKey, String propertyName, HttpServletResponse response
    ) throws IOException {
        final MetadataProperty property = this.providerAssetService.resolveAssetMetadataProperty(
            pid, resourceKey, propertyName
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

    @Override
    public BaseResponse unpublishAsset(String id) {
        try {
            final UnpublishAssetCommand command = UnpublishAssetCommand.builder()
                .userKey(this.currentUserKey())
                .publisherKey(this.currentUserKey())
                .pid(id)
                .build();

            this.providerAssetService.unpublishAsset(command);

            return RestResponse.success();
        } catch (final CatalogueServiceException ex) {
            if (ex.getCode() == CatalogueServiceMessageCode.ITEM_NOT_FOUND) {
                return RestResponse.notFound();
            }

            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

}
