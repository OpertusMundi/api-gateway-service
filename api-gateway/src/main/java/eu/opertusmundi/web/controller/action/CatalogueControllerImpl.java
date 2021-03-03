package eu.opertusmundi.web.controller.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.AssetAdditionalResourceEntity;
import eu.opertusmundi.common.domain.AssetResourceEntity;
import eu.opertusmundi.common.feign.client.BpmServerFeignClient;
import eu.opertusmundi.common.feign.client.CatalogueFeignClient;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueSearchQuery;
import eu.opertusmundi.common.model.catalogue.client.EnumCatalogueType;
import eu.opertusmundi.common.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.common.repository.AssetAdditionalResourceRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;
import eu.opertusmundi.web.controller.support.CatalogueUtils;
import eu.opertusmundi.web.repository.ProviderRepository;
import feign.FeignException;

@RestController
public class CatalogueControllerImpl extends BaseController implements CatalogueController {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueControllerImpl.class);

    private static final String WORKFLOW_CATALOGUE_HARVEST = "workflow-catalogue-harvest";
    
    @Autowired
    private ObjectProvider<BpmServerFeignClient> bpmClient;

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private CatalogueUtils catalogueUtils;
    
    @Autowired
    private AssetResourceRepository assetResourceRepository;

    @Autowired
    private AssetAdditionalResourceRepository assetAdditionalResourceRepository;

    @Override
    public RestResponse<?> findAll(CatalogueSearchQuery request) {
        try {
            // Catalogue service data page index is 1-based
            final ResponseEntity<CatalogueResponse<CatalogueCollection>> e = this.catalogueClient.getObject().findAll(
                request.getQuery(), null, request.getPage() + 1, request.getSize()
            );

            final CatalogueResponse<CatalogueCollection> catalogueResponse = e.getBody();

            if(!catalogueResponse.isSuccess()) {
                return RestResponse.failure();
            }

            // Process response
            final CatalogueClientCollectionResponse<CatalogueItemDto> response = this.catalogueUtils.createSeachResult(
                catalogueResponse, (item) -> new CatalogueItemDto(item), request.getPage(), request.getSize()
            );

            return response;
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // Convert 404 errors to empty results
            if (code == BasicMessageCode.NotFound) {
                return RestResponse.result(PageResultDto.<CatalogueItemDto>empty(request.toPageRequest()));
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            return RestResponse.error(code, "An error has occurred");
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<CatalogueItemDetailsDto> findOne(String id) {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneById(id);

            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if(!catalogueResponse.isSuccess()) {
                return RestResponse.failure();
            }

            // Convert feature to catalogue item
            final CatalogueItemDetailsDto item = new CatalogueItemDetailsDto(catalogueResponse.getResult());

            // Filter properties
            if (!this.isAuthenticated()) {
                item.setAutomatedMetadata(null);
            }

            // Inject publisher details
            final PublisherDto publisher = this.providerRepository.findOneByKey(item.getPublisherId()).toPublisherDto();

            item.setPublisher(publisher);
            
            // Consolidate data from asset repository
            List<AssetResourceEntity> resources = this.assetResourceRepository
                .findAllResourcesByAssetPid(item.getId());

            resources.stream()
                .map(AssetResourceEntity::toDto)
                .forEach(item.getResources()::add);
            
            List<AssetAdditionalResourceEntity> additionalResources = this.assetAdditionalResourceRepository
                .findAllResourcesByAssetPid(item.getId());
           
            item.getAdditionalResources().stream()
                .filter(r -> r.getType() == EnumAssetAdditionalResource.FILE)
                .forEach(r -> {
                    final AssetFileAdditionalResourceDto fileResource  = (AssetFileAdditionalResourceDto) r;
                    final AssetAdditionalResourceEntity resourceEntity = additionalResources.stream()
                        .filter(r1 -> r1.getKey().equals(fileResource.getId()))
                        .findFirst()
                        .orElse(null);

                        if (resourceEntity != null) {
                            fileResource.setModifiedOn(resourceEntity.getCreatedOn());
                            fileResource.setSize(resourceEntity.getSize());
                        }
                    });

            // Compute effective pricing models
            this.catalogueUtils.refreshPricingModels(item, catalogueResponse.getResult().getProperties().getPricingModels());

            return RestResponse.result(item);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            if (code == BasicMessageCode.NotFound) {
                return RestResponse.notFound();
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            return RestResponse.failure();
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<Void> harvestCatalogue(CatalogueHarvestCommandDto command, BindingResult validationResult) {
        // TODO: Create new service for catalogue methods
        try {
            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            // Check if workflow exists
            final String businessKey = String.format("%s-%s", this.currentUserKey(), command.getUrl());
            
            ProcessInstanceDto instance = this.findRunningInstance(businessKey);

            if (instance == null) {
                final StartProcessInstanceDto options = new StartProcessInstanceDto();

                final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

                // Set defaults
                if (command.getType() == null) {
                    command.setType(EnumCatalogueType.CSW);
                }
                // Set variables
                this.setStringVariable(variables, "userKey", this.currentUserKey());
                this.setStringVariable(variables, "catalogueUrl", command.getUrl());
                this.setStringVariable(variables, "catalogueType", command.getType().toString());

                options.setBusinessKey(businessKey);
                options.setVariables(variables);
                options.setWithVariablesInReturn(true);

                this.bpmClient.getObject().startProcessByKey(WORKFLOW_CATALOGUE_HARVEST, options);
            }
            
            return RestResponse.success();
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            logger.error("[Feign Client][BPM Server] Operation has failed", fex);

            return RestResponse.error(code, "An error has occurred");
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<?> findAllHarvested(String url, int pageIndex, int pageSize) {
        try {
            // Catalogue service data page index is 1-based
            final ResponseEntity<CatalogueResponse<CatalogueCollection>> e = this.catalogueClient.getObject().findAllHarvest(
                url, pageIndex + 1, pageSize
            );

            final CatalogueResponse<CatalogueCollection> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                return RestResponse.failure();
            }

            // Process response
            final CatalogueClientCollectionResponse<CatalogueItemDto> response = this.catalogueUtils.createSeachResult(
                catalogueResponse, (item) -> new CatalogueItemDto(item), pageIndex, pageSize, false
            );

            return response;
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // Convert 404 errors to empty results
            if (code == BasicMessageCode.NotFound) {
                return RestResponse.result(PageResultDto.<CatalogueItemDto>empty(PageRequestDto.of(pageIndex, pageSize)));
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            return RestResponse.error(code, "An error has occurred");
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }
    
    @Override
    public RestResponse<Void> importFromCatalogue(CatalogueHarvestImportCommandDto command, BindingResult validationResult) {
        return RestResponse.failure();
    }

    private void setStringVariable(Map<String, VariableValueDto> variables, String name, Object value) {
        this.setVariable(variables, "String", name, value);
    }

    private void setVariable(Map<String, VariableValueDto> variables, String type, String name, Object value) {
        final VariableValueDto v = new VariableValueDto();

        v.setValue(value);
        v.setType(type);

        variables.put(name, v);
    }

    private ProcessInstanceDto findRunningInstance(String businessKey) {
        final List<ProcessInstanceDto> instances = this.bpmClient.getObject().getInstance(businessKey);

        return instances.stream()
            .filter(i -> !i.isEnded())
            .findFirst()
            .orElse(null);
    }
    
}
