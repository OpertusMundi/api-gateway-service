package eu.opertusmundi.web.controller.action;

import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.account.EnumServiceBillingRecordSortField;
import eu.opertusmundi.common.model.payment.CheckoutServiceBillingCommandDto;
import eu.opertusmundi.common.model.payment.EnumBillableServiceType;
import eu.opertusmundi.common.model.payment.consumer.ConsumerServiceBillingCollectionDto;
import eu.opertusmundi.common.service.ServiceBillingService;
import eu.opertusmundi.common.service.ServiceUseStatsService;
import eu.opertusmundi.common.service.mangopay.PayInService;

@RestController
public class ConsumerServiceBillingControllerImpl extends BaseController implements ConsumerServiceBillingController {

    private final PayInService         paymentService;
    private final ServiceUseStatsService serviceUseStatsService;
    private final ServiceBillingService  serviceBillingService;

    @Autowired
    public ConsumerServiceBillingControllerImpl(
        PayInService paymentService,
        ServiceUseStatsService serviceUseStatsService,
        ServiceBillingService serviceBillingService
    ) {
        this.paymentService         = paymentService;
        this.serviceUseStatsService = serviceUseStatsService;
        this.serviceBillingService  = serviceBillingService;
    }

    @Override
    public RestResponse<?> findAll(
        UUID serviceKey, EnumBillableServiceType type, Set<EnumPayoffStatus> status,
        int pageIndex, int pageSize,
        EnumServiceBillingRecordSortField orderBy, EnumSortingOrder order
    ) {
        final var ownerKey = this.currentUserKey();
        final var result   = this.serviceBillingService.findAllServiceBillingRecords(
            EnumView.CONSUMER, type, ownerKey, null /* provider key */, serviceKey, status, pageIndex, pageSize, orderBy, order
        );

        return ConsumerServiceBillingCollectionDto.of(result);
    }

    @Override
    public RestResponse<?> findOne(UUID key) {
        final var result = this.serviceBillingService.findOneServiceBillingRecord(EnumView.CONSUMER, key);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> checkout(@Valid CheckoutServiceBillingCommandDto command) {
        command.setUserKey(this.currentUserKey());

        final var result = this.paymentService.preparePayInFromServiceBillingRecords(command);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> getUsageStatistics(EnumBillableServiceType serviceType, UUID serviceKey, Integer year, Integer month) {
        final var userKey = this.currentUserKey();
        final var result  = serviceUseStatsService.getUseStats(serviceType, userKey, serviceKey, year, month);

        return RestResponse.result(result);
    }

}
