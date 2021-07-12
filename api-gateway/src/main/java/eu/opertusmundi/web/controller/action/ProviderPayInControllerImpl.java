package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.payment.EnumPayInItemSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInItemDto;
import eu.opertusmundi.common.service.PaymentService;

@RestController
public class ProviderPayInControllerImpl extends BaseController implements ProviderPayInController {

    @Autowired
    private PaymentService paymentService;

    @Override
    public RestResponse<?> findOnePayInItem(UUID payInKey, Integer index) {
        final ProviderPayInItemDto result = this.paymentService.getProviderPayInItem(this.currentUserId(), payInKey, index);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findAllProviderPayInItems(
        EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayInItemSortField orderBy, EnumSortingOrder order
    ) {
        final UUID                                userKey = this.currentUserKey();
        final PageResultDto<ProviderPayInItemDto> result  = this.paymentService.findAllProviderPayInItems(
            userKey, status, pageIndex, pageSize, orderBy, order
        );

        return RestResponse.result(result);
    }

}
