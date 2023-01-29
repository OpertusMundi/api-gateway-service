package eu.opertusmundi.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.discovery.client.ClientJoinableResultDto;
import eu.opertusmundi.common.model.discovery.client.ClientRelatedResultDto;
import eu.opertusmundi.common.service.DiscoveryService;

@ConditionalOnProperty(name = "opertusmundi.feign.discovery.url", matchIfMissing = true)
@RestController
public class DiscoveryControllerImpl extends BaseController implements DiscoveryController {

    private final DiscoveryService discoveryService;

    @Autowired
    public DiscoveryControllerImpl(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public RestResponse<ClientJoinableResultDto> findJoinable(String id) {
        try {
            final var result = discoveryService.findJoinable(id);

            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<ClientRelatedResultDto> findRelated(String source, String[] target) {
        try {
            final var result = discoveryService.findRelated(source, target);

            return RestResponse.result(result);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

}
