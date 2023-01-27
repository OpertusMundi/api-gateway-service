package eu.opertusmundi.web.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.web.controller.action.BaseController;

@RestController("ApiCatalogueController")
public class CatalogueControllerImpl extends BaseController implements CatalogueController {

    private final CatalogueService catalogueService;

    @Autowired
    public CatalogueControllerImpl(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @Override
    public RestResponse<?> findAll(ElasticAssetQuery request) {
        return this.findAllImpl(request);
    }

    private RestResponse<?> findAllImpl(ElasticAssetQuery request) {
        try {
            final CatalogueResult<CatalogueItemDto> result = catalogueService.findAllElastic(this.createContext(), request);

            return CatalogueClientCollectionResponse.of(result.getResult(), result.getPublishers());
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<CatalogueItemDetailsDto> findOne(String id) {
        try {
            final CatalogueItemDetailsDto item = catalogueService.findOne(
                this.createContext(), id, this.currentUserParentKey(), this.isAuthenticated()
            );

            return item == null ? RestResponse.notFound() : RestResponse.result(item);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }
}
