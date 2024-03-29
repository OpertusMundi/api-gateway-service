package eu.opertusmundi.web.controller.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.AccountAssetDto;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.EnumSubscriptionStatus;
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.asset.EnumConsumerSubSortField;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.service.ConsumerAssetService;
import eu.opertusmundi.common.util.StreamUtils;
import eu.opertusmundi.web.controller.action.BaseController;

@RestController("ApiConsumerAssetController")
public class ConsumerAssetControllerImpl extends BaseController implements ConsumerAssetController {

    private final ConsumerAssetService consumerAssetService;

    @Autowired
    public ConsumerAssetControllerImpl(ConsumerAssetService consumerAssetService) {
        this.consumerAssetService = consumerAssetService;
    }

    @Override
    public RestResponse<?> findAllAssets(
        EnumAssetType type, int pageIndex, int pageSize, EnumConsumerAssetSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID                           userKey = this.currentUserKey();
            final PageResultDto<AccountAssetDto> result  = this.consumerAssetService.findAllAssets(
                userKey, type, pageIndex, pageSize, orderBy, order
            );

            StreamUtils.from(result.getItems())
                .map(AccountAssetDto::getItem)
                .forEach(this::filterCatalogueItemProps);

            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> findAllSubscriptions(
        EnumSpatialDataServiceType type,
        int pageIndex, int pageSize, EnumConsumerSubSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID                                  userKey = this.currentUserKey();
            final PageResultDto<AccountSubscriptionDto> result  = this.consumerAssetService.findAllSubscriptions(
                userKey, EnumSubscriptionStatus.ACTIVE, type, pageIndex, pageSize, orderBy, order
            );

            StreamUtils.from(result.getItems())
                .map(AccountSubscriptionDto::getItem)
                .forEach(this::filterCatalogueItemProps);

            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> findOneSubscription(UUID key) {
        try {
            final UUID                   userKey = this.currentUserKey();
            final AccountSubscriptionDto result  = this.consumerAssetService.findSubscription(userKey, key);

            if (result == null || result.getStatus() != EnumSubscriptionStatus.ACTIVE) {
                return RestResponse.notFound();
            }
            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadResource(
        String pid, String resourceKey, HttpServletResponse response
    ) throws IOException {
        try {
            final UUID userKey = this.currentUserKey();

            final FileResourceDto resource = this.consumerAssetService.resolveResourcePath(userKey, pid, resourceKey);
            final Path            path     = resource.getRelativePath();
            final File            file     = path.toFile();
            final String          fileName = resource.getFileName();

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setHeader("Content-Disposition", String.format("attachment; filename=%s", fileName));
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
        } catch (final ServiceException ex) {
            final HttpStatus httpStatus = ex.getCode() instanceof BasicMessageCode
                ? ((BasicMessageCode) ex.getCode()).getHttpStatus()
                : HttpStatus.INTERNAL_SERVER_ERROR;

            return new ResponseEntity<>(null, httpStatus);
        }
    }

    private void filterCatalogueItemProps(CatalogueItemDetailsDto item) {
        item.resetAutomatedMetadata();
        item.resetContract();

        item.getPricingModels().clear();
    }
}
