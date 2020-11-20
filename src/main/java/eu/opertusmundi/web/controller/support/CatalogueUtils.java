package eu.opertusmundi.web.controller.support;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import brave.Span;
import brave.Tracer;
import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.web.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.web.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.web.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.web.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.web.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.web.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.FixedPricingModelDto;
import eu.opertusmundi.web.model.pricing.FreePricingModelDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelCommandDto;
import eu.opertusmundi.web.model.pricing.SubscriptionPricingModelDto;
import eu.opertusmundi.web.repository.ProviderRepository;

@Service
public class CatalogueUtils {

    // TODO: Set from configuration
    private final BigDecimal tax = new BigDecimal(24);

    @Autowired
    private Tracer tracer;

    @Autowired
    private ProviderRepository providerRepository;

    /**
     * Convert a catalogue response to an API Gateway response
     *
     * @param catalogueResponse
     * @param converter
     * @param pageIndex
     * @param pageSize
     * @return
     */
    public <T extends CatalogueItemDto> CatalogueClientCollectionResponse<T> createSeachResult(
        CatalogueResponse<CatalogueCollection> catalogueResponse,
        Function<CatalogueFeature, T> converter,
        int pageIndex, int pageSize
    ) {
        // Convert features to items
        final CatalogueCollection features = catalogueResponse.getResult();

        final List<T> items = features.getItems().stream()
            .map(item -> {
                final T dto = converter.apply(item);

                // Compute effective pricing models
                this.refreshPricingModels(dto, item.getProperties().getPricingModels());

                return dto;
            })
            .collect(Collectors.toList());


        final PageResultDto<T> result = PageResultDto.of(
            pageIndex,
            pageSize,
            items,
            features.getTotal()
        );

        // Get all publishers in the result
        final Span span = this.tracer.nextSpan().name("database-publisher").start();

        final List<PublisherDto> publishers;

        try {
            final UUID[] publisherKeys = items.stream().map(i -> i.getPublisherId()).distinct().toArray(UUID[]::new);

            publishers = this.providerRepository.findAllByKey(publisherKeys).stream()
                .map(AccountEntity::toPublisherDto)
                .collect(Collectors.toList());

            // TODO: Check that all publishers exists
            Assert.isTrue(publisherKeys.length == publishers.size(), "All publishers must exist!");
        } finally {
            span.finish();
        }

        return new CatalogueClientCollectionResponse<T>(result, publishers);
    }

    /**
     * Compute pricing models effective values for a catalogue item
     *
     * @param item
     * @param models
     */
    public void refreshPricingModels(CatalogueItemDto item, List<BasePricingModelCommandDto> models) {
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
