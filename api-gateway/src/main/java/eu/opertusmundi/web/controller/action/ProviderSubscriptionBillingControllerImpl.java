package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingSortField;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingStatus;
import eu.opertusmundi.common.model.payment.provider.ProviderSubscriptionBillingCollectionDto;
import eu.opertusmundi.common.service.SubscriptionBillingService;

@RestController
public class ProviderSubscriptionBillingControllerImpl extends BaseController implements ProviderSubscriptionBillingController {

    private final SubscriptionBillingService subscriptionBillingService;

    @Autowired
    public ProviderSubscriptionBillingControllerImpl(SubscriptionBillingService subscriptionBillingService) {
        this.subscriptionBillingService = subscriptionBillingService;
    }

    @Override
    public RestResponse<?> findAll(
        UUID subscriptionKey, Set<EnumSubscriptionBillingStatus> status, int pageIndex, int pageSize,
        EnumSubscriptionBillingSortField orderBy, EnumSortingOrder order
    ) {
        final var providerKey = this.currentUserKey();
        final var result      = this.subscriptionBillingService.findAllSubscriptionBillingRecords(
            EnumView.PROVIDER, null /* consumer key */, providerKey, subscriptionKey, status, pageIndex, pageSize, orderBy, order
        );


        return ProviderSubscriptionBillingCollectionDto.of(result);
    }

    @Override
    public RestResponse<?> findOne(UUID key) {
        final var result = this.subscriptionBillingService.findOneSubscriptionBillingRecord(EnumView.PROVIDER, key);

        return RestResponse.result(result);
    }

}
