package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.payment.EnumPayOutSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.service.PaymentService;

@RestController
public class ProviderPayOutControllerImpl extends BaseController implements ProviderPayOutController {

    @Autowired
    private PaymentService paymentService;

    @Override
    public RestResponse<?> findOnePayOut(UUID payOutKey) {
        final PayOutDto result = this.paymentService.getProviderPayOut(this.currentUserParentId(), payOutKey);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findAllProviderPayOuts(
        EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayOutSortField orderBy, EnumSortingOrder order
    ) {
        final UUID                    userKey = this.currentUserParentKey();
        final PageResultDto<PayOutDto> result  = this.paymentService.findAllProviderPayOuts(
            userKey, status, pageIndex, pageSize, orderBy, order
        );

        return RestResponse.result(result);
    }

}
