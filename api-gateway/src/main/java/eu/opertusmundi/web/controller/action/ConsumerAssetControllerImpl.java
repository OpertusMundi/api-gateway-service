package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountAssetDto;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.asset.EnumConsumerSubSortField;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.service.ConsumerAssetService;

@RestController
public class ConsumerAssetControllerImpl extends BaseController implements ConsumerAssetController {

    @Autowired
    private ConsumerAssetService consumerAssetService;

    @Override
    public RestResponse<?> findAllAssets(
        EnumAssetType type, int pageIndex, int pageSize, EnumConsumerAssetSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID userKey = this.currentUserKey();

            final PageResultDto<AccountAssetDto> result = this.consumerAssetService.findAllAssets(
                userKey, type, pageIndex, pageSize, orderBy, order
            );

            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> findAllSubscriptions(
        EnumSpatialDataServiceType type, int pageIndex, int pageSize, EnumConsumerSubSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID userKey = this.currentUserKey();

            final PageResultDto<AccountSubscriptionDto> result = this.consumerAssetService.findAllSubscriptions(
                userKey, type, pageIndex, pageSize, orderBy, order
            );

            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> findSubscription(UUID orderKey) {
        try {
            final UUID userKey = this.currentUserKey();

            final AccountSubscriptionDto result = this.consumerAssetService.findSubscription(userKey, orderKey);

            if (result == null) {
                return RestResponse.notFound();
            }
            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

}
