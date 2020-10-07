package eu.opertusmundi.web.controller.action;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import brave.Span;
import brave.Tracer;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.QueryResultPage;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.feign.client.CatalogueFeignClient;
import eu.opertusmundi.web.model.catalogue.client.CatalogueAddItemCommandDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueSearchQuery;
import eu.opertusmundi.web.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.web.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.web.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.FixedPricingModelDto;
import eu.opertusmundi.web.model.pricing.FreePricingModelDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelDto;
import eu.opertusmundi.web.repository.AssetProviderRepository;
import feign.FeignException;

@RestController
public class CatalogueControllerImpl extends BaseController implements CatalogueController {

    // TODO: Set from configuration
    private final BigDecimal tax = new BigDecimal(24);

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Autowired
    private AssetProviderRepository assetProviderRepository;

    @Autowired
    Tracer tracer;

    @Override
    public RestResponse<?> find(CatalogueSearchQuery request) {
        // Query service
        ResponseEntity<CatalogueResponse<CatalogueCollection>> e;

        try {
            // Catalogue service data page index is 1-based
            e = this.catalogueClient.getObject().find(
                request.getQuery(), request.getPage() + 1, request.getSize()
            );
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // Convert 404 errors to empty results
            if (code == BasicMessageCode.NotFound) {
                return RestResponse.result(
                    new QueryResultPage<CatalogueItemDto>(request.toPageRequest())
                );
            }

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }

        final CatalogueResponse<CatalogueCollection> catalogueResponse = e.getBody();

        if(!catalogueResponse.isSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        // Convert features to items
        final CatalogueCollection features = catalogueResponse.getResult();

        final List<CatalogueItemDto> items = features.getItems().stream()
            .map(item -> {
                final CatalogueItemDto dto = new CatalogueItemDto(item);

                // Compute effective pricing models
                this.refreshPricingModels(dto, item.getProperties().getPricingModels());

                return dto;
            })
            .collect(Collectors.toList());


        final QueryResultPage<CatalogueItemDto> result = new QueryResultPage<CatalogueItemDto>(
            request.toPageRequest(),
            features.getTotal(),
            items
        );

        // Get all publishers in the result
        final Span span = this.tracer.nextSpan().name("database-publisher").start();

        List<PublisherDto> publishers;

        try {
            final UUID[] publisherKeys = items.stream().map(i -> i.getPublisherId()).distinct().toArray(UUID[]::new);

            publishers = this.assetProviderRepository.findAllByKey(publisherKeys).stream()
                .map(AccountEntity::toPublisherDto)
                .collect(Collectors.toList());

            // TODO: Check that all publishers exists
            Assert.isTrue(publisherKeys.length == publishers.size(), "All publishers must exist!");
        } catch(final Exception ex) {
            // TODO: Add logging
            return RestResponse.failure();
        } finally {
            span.finish();
        }

        final CatalogueClientCollectionResponse response = new CatalogueClientCollectionResponse(result, publishers);

        return response;
    }

    @Override
    public RestResponse<CatalogueItemDetailsDto> findOne(UUID id) {
        ResponseEntity<CatalogueResponse<CatalogueFeature>> e;

        // Query catalogue service
        try {
            e = this.catalogueClient.getObject().findOneById(id);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            if (code == BasicMessageCode.NotFound) {
                return RestResponse.notFound();
            }

            // TODO: Add logging ...
            return RestResponse.error(code, "An error has occurred");
        }

        final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

        if(!catalogueResponse.isSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        // Convert feature to catalogue item
        final CatalogueItemDetailsDto item = new CatalogueItemDetailsDto(catalogueResponse.getResult());

        // Inject publisher details
        final PublisherDto publisher = this.assetProviderRepository.findOneByKey(item.getPublisherId()).toPublisherDto();

        item.setPublisher(publisher);

        // Compute effective pricing models
        this.refreshPricingModels(item, catalogueResponse.getResult().getProperties().getPricingModels());

        return RestResponse.result(item);
    }

    @Override
    public RestResponse<Void> create(CatalogueAddItemCommandDto command) {
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
        final ResponseEntity<Void> response = this.catalogueClient.getObject().create(feature);

        if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
            return RestResponse.success();
        }

        return RestResponse.error(BasicMessageCode.InternalServerError, "Record creation failed");
    }

    // TODO : Move to new service

    private void refreshPricingModels(CatalogueItemDto item, List<BasePricingModelCommandDto> models) {
        models.forEach(m -> {
            final UUID key = m.getKey();

            switch (m.getType()) {
                case FREE :
                    item.getPricingModels().add(new FreePricingModelDto(
                        key,
                        this.tax
                    ));
                    break;
                case FIXED :
                    final FixedPricingModelCommandDto fixed = (FixedPricingModelCommandDto) m;

                    item.getPricingModels().add(new FixedPricingModelDto(
                        key,
                        fixed.getTotalPriceExcludingTax(),
                        this.tax,
                        fixed.isIncludesUpdates(),
                        fixed.getYearsOfUpdates()
                    ));
                    break;
                case SUBSCRIPTION :
                    final SubscriptionPricingModelCommandDto subscription = (SubscriptionPricingModelCommandDto) m;

                    item.getPricingModels().add(new SubscriptionPricingModelDto(
                        key,
                        subscription.getDuration(),
                        this.tax,
                        subscription.getMonthlyPrice()
                    ));
                    break;
                default :
                    // Do nothing
                    break;
            }
        });
    }

}
