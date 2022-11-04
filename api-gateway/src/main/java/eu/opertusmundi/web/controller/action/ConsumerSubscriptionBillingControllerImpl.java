package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingSortField;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingStatus;
import eu.opertusmundi.common.model.payment.CheckoutSubscriptionBillingCommandDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerSubscriptionBillingCollectionDto;
import eu.opertusmundi.common.service.SubscriptionBillingService;
import eu.opertusmundi.common.service.mangopay.PaymentService;

@RestController
public class ConsumerSubscriptionBillingControllerImpl extends BaseController implements ConsumerSubscriptionBillingController {

    private final PaymentService             paymentService;
    private final SubscriptionBillingService subscriptionBillingService;


    @Autowired
    public ConsumerSubscriptionBillingControllerImpl(
        PaymentService             paymentService,
        SubscriptionBillingService subscriptionBillingService
    ) {
        this.paymentService             = paymentService;
        this.subscriptionBillingService = subscriptionBillingService;
    }

    @Override
    public RestResponse<?> findAll(
        UUID subscriptionKey, Set<EnumSubscriptionBillingStatus> status, int pageIndex, int pageSize,
        EnumSubscriptionBillingSortField orderBy, EnumSortingOrder order
    ) {
        final var consumerKey = this.currentUserKey();
        final var result      = this.subscriptionBillingService.findAllSubscriptionBillingRecords(
            EnumView.CONSUMER, consumerKey, null /* provider key */, subscriptionKey, status, pageIndex, pageSize, orderBy, order
        );


        return ConsumerSubscriptionBillingCollectionDto.of(result);
    }

    @Override
    public RestResponse<?> findOne(UUID key) {
        final var result = this.subscriptionBillingService.findOneSubscriptionBillingRecord(EnumView.CONSUMER, key);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> checkout(@Valid CheckoutSubscriptionBillingCommandDto command) {
        command.setUserKey(this.currentUserKey());

        final var result = this.paymentService.preparePayInFromSubscriptionBillingRecords(command);

        return RestResponse.result(result);
    }

}
