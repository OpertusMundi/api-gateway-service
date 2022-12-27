package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.account.EnumServiceBillingRecordSortField;
import eu.opertusmundi.common.model.payment.EnumBillableServiceType;
import eu.opertusmundi.common.model.payment.provider.ProviderServiceBillingCollectionDto;
import eu.opertusmundi.common.service.ServiceBillingService;
import eu.opertusmundi.common.service.ServiceUseStatsService;

@RestController
public class ProviderServiceBillingControllerImpl extends BaseController implements ProviderServiceBillingController {

    private final ServiceUseStatsService serviceUseStatsService;
    private final ServiceBillingService  serviceBillingService;

    @Autowired
    public ProviderServiceBillingControllerImpl(
        ServiceUseStatsService serviceUseStatsService, ServiceBillingService serviceBillingService
    ) {
        this.serviceUseStatsService = serviceUseStatsService;
        this.serviceBillingService  = serviceBillingService;
    }

    @Override
    public RestResponse<?> findAll(
        UUID subscriptionKey, Set<EnumPayoffStatus> status, int pageIndex, int pageSize,
        EnumServiceBillingRecordSortField orderBy, EnumSortingOrder order
    ) {
        final var providerKey = this.currentUserKey();
        final var result      = this.serviceBillingService.findAllServiceBillingRecords(
            EnumView.PROVIDER, EnumBillableServiceType.SUBSCRIPTION,
            null /* owner key */, providerKey, subscriptionKey, status,
            pageIndex, pageSize, orderBy, order
        );

        return ProviderServiceBillingCollectionDto.of(result);
    }

    @Override
    public RestResponse<?> findOne(UUID key) {
        final var result = this.serviceBillingService.findOneServiceBillingRecord(EnumView.PROVIDER, key);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> getUsageStatistics(UUID consumerKey, UUID subscriptionKey, Integer year, Integer month) {
        final var result = serviceUseStatsService.getUseStats(
            EnumBillableServiceType.SUBSCRIPTION, consumerKey, subscriptionKey, year, month
        );

        return RestResponse.result(result);
    }

}
