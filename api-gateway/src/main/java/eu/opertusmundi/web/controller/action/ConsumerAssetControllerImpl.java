package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.EnumType;
import eu.opertusmundi.common.model.dto.AccountAssetDto;
import eu.opertusmundi.common.model.dto.EnumSortingOrder;
import eu.opertusmundi.common.service.ConsumerAssetService;

@RestController
public class ConsumerAssetControllerImpl extends BaseController implements ConsumerAssetController {

    @Autowired
    private ConsumerAssetService consumerAssetService;

    @Override
    public RestResponse<?> findAll(
        EnumType type, int pageIndex, int pageSize, EnumConsumerAssetSortField orderBy, EnumSortingOrder order
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

}
