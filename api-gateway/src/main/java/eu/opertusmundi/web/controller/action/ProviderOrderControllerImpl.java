package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.EnumOrderSortField;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderConfirmCommandDto;
import eu.opertusmundi.common.model.order.OrderException;
import eu.opertusmundi.common.model.order.OrderShippingCommandDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.OrderFulfillmentService;

@RestController
public class ProviderOrderControllerImpl extends BaseController implements ProviderOrderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderOrderControllerImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderFulfillmentService orderFulfillmentService;

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
            true, false
        );

        final long                            count   = p.getTotalElements();
        final List<ProviderOrderDto>          records = p.stream().collect(Collectors.toList());
        final PageResultDto<ProviderOrderDto> result  = PageResultDto.of(pageIndex, pageSize, records, count);

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse confirmOrder(UUID orderKey, OrderConfirmCommandDto command) {
        try {
            command.setOrderKey(orderKey);
            command.setPublisherKey(this.currentUserKey());

            if (command.isRejected()) {
                this.orderFulfillmentService.rejectOrder(command);
            } else {
                this.orderFulfillmentService.acceptOrder(command);
            }

            return this.findOne(orderKey);
        } catch (final AssetDraftException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse shipOrder(UUID orderKey, OrderShippingCommandDto command) {
        try {
            command.setOrderKey(orderKey);
            command.setPublisherKey(this.currentUserKey());

            this.orderFulfillmentService.sendOrderByProvider(command);

            return this.findOne(orderKey);
        } catch (final OrderException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

}
