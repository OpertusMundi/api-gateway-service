package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.common.service.ProviderRegistrationService;
import eu.opertusmundi.web.controller.support.CatalogueUtils;
import eu.opertusmundi.web.feign.client.CatalogueFeignClient;
import eu.opertusmundi.web.model.catalogue.client.CatalogueAddItemCommandDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientSetStatusCommandDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDraftDetailsDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDraftDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.web.model.catalogue.client.EnumDraftStatus;
import eu.opertusmundi.web.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.web.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.web.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.web.repository.ProviderRepository;
import eu.opertusmundi.web.validation.ProviderValidator;
import feign.FeignException;

@RestController
public class ProviderControllerImpl extends BaseController implements ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderControllerImpl.class);

    @Autowired
    private ProviderRegistrationService providerService;

    @Autowired
    private ProviderValidator providerValidator;

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Autowired
    private CatalogueUtils catalogueUtils;

    @Autowired
    private ProviderRepository providerRepository;

    @Override
    public RestResponse<AccountProfileDto> updateRegistration(
        ProviderProfessionalCommandDto command, BindingResult validationResult
    ) {
        return this.update(command, validationResult, true);
    }

    @Override
    public RestResponse<AccountProfileDto> submitRegistration(
        ProviderProfessionalCommandDto command, BindingResult validationResult
    ) {
        return this.update(command, validationResult, false);
    }

    @Override
    public RestResponse<AccountProfileDto> cancelRegistration() {
        final UUID userKey = this.authenticationFacade.getCurrentUserKey();

        try {
            final AccountDto account = this.providerService.cancelRegistration(userKey);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error("Provider update has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    private RestResponse<AccountProfileDto> update(
        ProviderProfessionalCommandDto command, BindingResult validationResult, boolean draft
    ) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setUserId(id);

        this.providerValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = draft ?
                this.providerService.updateRegistration(command) :
                this.providerService.submitRegistration(command);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error("Provider update has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    @Override
    public RestResponse<?> findAllDraft(EnumDraftStatus status, int pageIndex, int pageSize) {
        // Query service
        ResponseEntity<CatalogueResponse<CatalogueCollection>> e;

        try {
            final UUID publisherId = this.currentUserKey();

            // Catalogue service data page index is 1-based
            e = this.catalogueClient.getObject().findAllDraft(
                publisherId, status.getValue(), pageIndex + 1, pageSize
            );

            // Check if catalogue response is successful
            final CatalogueResponse<CatalogueCollection> catalogueResponse = e.getBody();

            if(!catalogueResponse.isSuccess()) {
                return RestResponse.failure();
            }

            // Process response
            final CatalogueClientCollectionResponse<CatalogueItemDraftDto> response = this.catalogueUtils.createSeachResult(
                catalogueResponse, (item)-> new CatalogueItemDraftDto(item), pageIndex, pageSize
            );

            return response;
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            if (code == BasicMessageCode.NotFound) {
                return RestResponse.result(PageResultDto.<CatalogueItemDto>empty(new PageRequestDto(pageIndex, pageSize)));
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            return RestResponse.failure();
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<Void> createDraft(CatalogueAddItemCommandDto command) {
        try {
            // TODO : id must be created by the PID service

            // Inject provider (current user) key
            command.setPublisherId(this.currentUserKey());

            // Create feature
            final CatalogueFeature feature = command.toFeature();

            // Compute effective pricing models
            final List<BasePricingModelCommandDto> featurePricingModels = feature.getProperties().getPricingModels();

            command.getPricingModels().stream().forEach(m-> {
                // Always override the key with a value generated at the server
                m.setKey(UUID.randomUUID());

                featurePricingModels.add(m);
            });

            // Insert new asset
            this.catalogueClient.getObject().createDraft(feature);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public RestResponse<CatalogueItemDraftDetailsDto> findOneDraft(UUID id) {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneDraftById(id);

            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if(!catalogueResponse.isSuccess()) {
                return RestResponse.failure();
            }

            // Convert feature to catalogue item
            final CatalogueItemDraftDetailsDto item = new CatalogueItemDraftDetailsDto(catalogueResponse.getResult());

            // Inject publisher details
            final PublisherDto publisher = this.providerRepository.findOneByKey(item.getPublisherId()).toPublisherDto();

            item.setPublisher(publisher);

            // Compute effective pricing models
            this.catalogueUtils.refreshPricingModels(item, catalogueResponse.getResult().getProperties().getPricingModels());

            return RestResponse.result(item);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // Convert 404 errors to empty results
            if (code == BasicMessageCode.NotFound) {
                return RestResponse.notFound();
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            return RestResponse.error(code, "An error has occurred");
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<CatalogueItemDetailsDto> updateDraft(UUID id, CatalogueAddItemCommandDto command) {
        try {
            final UUID publisherId = this.currentUserKey();

            // Inject asset and provider identifiers
            command.setId(id);
            command.setPublisherId(publisherId);

            // Create feature
            final CatalogueFeature feature = command.toFeature();

            // Compute effective pricing models
            final List<BasePricingModelCommandDto> featurePricingModels = feature.getProperties().getPricingModels();

            command.getPricingModels().stream().forEach(m-> {
                // Always override the key with a value generated at the server
                m.setKey(UUID.randomUUID());

                featurePricingModels.add(m);
            });

            // Update draft
            this.catalogueClient.getObject().updateDraft(id, feature);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse setDraftStatus(UUID id, CatalogueClientSetStatusCommandDto command) {
        try {
            this.catalogueClient.getObject().setDraftStatus(id, command.getStatus().getValue());

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse deleteDraft(UUID id) {
        try {
            this.catalogueClient.getObject().deleteDraft(id);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }

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
