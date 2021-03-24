package eu.opertusmundi.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueAssetQuery;
import eu.opertusmundi.common.service.CatalogueService;

@RestController
public class CatalogueControllerImpl extends BaseController implements CatalogueController {

    @Autowired
    private CatalogueService catalogueService;
    
    @Override
    public RestResponse<?> findAll(CatalogueAssetQuery request) {
        try {
            final CatalogueResult<CatalogueItemDto> result = catalogueService.findAll(request);

            return CatalogueClientCollectionResponse.of(result.getResult(), result.getPublishers());
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<CatalogueItemDetailsDto> findOne(String id) {
        try {
            final CatalogueItemDetailsDto item = catalogueService.findOne(id, this.currentUserKey(), this.isAuthenticated());

            return item == null ? RestResponse.notFound() : RestResponse.result(item);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<Void> harvestCatalogue(CatalogueHarvestCommandDto command, BindingResult validationResult) {
        try {
            command.setUserKey(this.currentUserKey());

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            this.catalogueService.harvestCatalogue(command);

            return RestResponse.success();
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> findAllHarvested(String url, String query, int pageIndex, int pageSize) {
        try {
            final CatalogueResult<CatalogueItemDto> result = this.catalogueService.findAllHarvested(url, query, pageIndex, pageSize);

            return CatalogueClientCollectionResponse.of(result.getResult(), result.getPublishers());
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }
    
    @Override
    public RestResponse<Void> importFromCatalogue(CatalogueHarvestImportCommandDto command, BindingResult validationResult) {
        this.catalogueService.importFromCatalogue(command);

        return RestResponse.failure();
    }

}
