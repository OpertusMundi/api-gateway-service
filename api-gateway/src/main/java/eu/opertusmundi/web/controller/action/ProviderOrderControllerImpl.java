package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.EnumOrderSortField;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.repository.OrderRepository;

@RestController
public class ProviderOrderControllerImpl extends BaseController implements ProviderOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public RestResponse<?> findOne(UUID orderKey) {
        final Optional<ProviderOrderDto> r = this.orderRepository.findOrderObjectByKeyAndProvider(this.currentUserKey(), orderKey);
        if (r.isPresent()) {
            return RestResponse.result(r.get());
        }
        return RestResponse.notFound();
    }

    @Override
    public RestResponse<?> findAll(
        Set<EnumOrderStatus> status, String referenceNumber,
        int pageIndex, int pageSize,
        EnumOrderSortField orderBy, EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        final Page<ProviderOrderDto> p = this.orderRepository.findAllObjectsForProvider(
            this.currentUserKey(),
            referenceNumber,
            status,
            pageRequest,
            false, false
        );

        final long                            count   = p.getTotalElements();
        final List<ProviderOrderDto>          records = p.stream().collect(Collectors.toList());
        final PageResultDto<ProviderOrderDto> result  = PageResultDto.of(pageIndex, pageSize, records, count);

        return RestResponse.result(result);
    }

}
