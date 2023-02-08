package eu.opertusmundi.web.controller.action;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueJoinableItemDto;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.ProviderAssetService;

@RestController
public class CatalogueControllerImpl extends BaseController implements CatalogueController {

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private ProviderAssetService providerAssetService;

    @Override
    public RestResponse<?> findAll(ElasticAssetQuery request) {
        return this.findAllImpl(request);
    }

    @Override
    public RestResponse<?> findAllAutocomplete(ElasticAssetQuery request) {
        request.setAutocomplete(true);
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
    public RestResponse<?> findAllRelatedAssets(String id) {
        try {
            final CatalogueResult<CatalogueItemDto> result = catalogueService.findAllRelatedAssets(this.createContext(), id);

            return CatalogueClientCollectionResponse.of(result.getResult(), result.getPublishers());
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> findAllRelatedBundles(String id) {
        try {
            final CatalogueResult<CatalogueItemDto> result = catalogueService.findAllRelatedBundles(this.createContext(), id);

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

    @Override
    public RestResponse<CatalogueItemDetailsDto> findOne(String id, String version) {
        try {
            final CatalogueItemDetailsDto item = catalogueService.findOne(
                this.createContext(), id, version, this.currentUserParentKey(), this.isAuthenticated()
            );

            return item == null ? RestResponse.notFound() : RestResponse.result(item);
        } catch (final CatalogueServiceException ex) {
            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<CatalogueJoinableItemDto> findOneJoinable(String id) {
        try {
            final CatalogueJoinableItemDto item = catalogueService.findOneJoinable(id);

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
    public RestResponse<?> importFromCatalogue(
        CatalogueHarvestImportCommandDto command, BindingResult validationResult
    ) {
        try {
            command.setPublisherKey(this.currentUserKey());

            final Map<String, AssetDraftDto> result = providerAssetService.importFromCatalogue(command);

            return RestResponse.result(result);
        } catch (final AssetDraftException ex) {
            return RestResponse.failure(ex.getCode(), ex.getMessage());
        }
    }

}
