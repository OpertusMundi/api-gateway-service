package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.feign.client.CatalogueFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import feign.FeignException;

@RestController
public class ProviderAssetControllerImpl implements ProviderAssetController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderAssetController.class);

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Override
    public BaseResponse deleteAsset(UUID id) {
        try {
            this.catalogueClient.getObject().deletePublished(id);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

}
