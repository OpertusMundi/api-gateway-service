package eu.opertusmundi.web.controller.action;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.order.AcceptOrderContractCommand;
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.EnumOrderSortField;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderDeliveryCommand;
import eu.opertusmundi.common.model.order.OrderException;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.service.OrderFulfillmentService;
import eu.opertusmundi.common.service.invoice.InvoiceFileManager;

@RestController
public class ConsumerOrderControllerImpl extends BaseController implements ConsumerOrderController {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerOrderControllerImpl.class);

    private final InvoiceFileManager      invoiceFileManager;
    private final OrderRepository         orderRepository;
    private final OrderFulfillmentService orderFulfillmentService;

    @Autowired
    public ConsumerOrderControllerImpl(
        InvoiceFileManager invoiceFileManager,
        OrderRepository orderRepository,
        OrderFulfillmentService orderFulfillmentService
    ) {
        this.invoiceFileManager      = invoiceFileManager;
        this.orderRepository         = orderRepository;
        this.orderFulfillmentService = orderFulfillmentService;
    }

    @Override
    public RestResponse<?> findOne(UUID orderKey) {
        final Optional<ConsumerOrderDto> r = this.orderRepository.findObjectByKeyAndConsumerAndStatusNotCreated(this.currentUserKey(), orderKey);
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

        final Page<ConsumerOrderDto> p = this.orderRepository.findAllObjectsForConsumer(
            this.currentUserKey(),
            referenceNumber,
            status,
            pageRequest,
            true, false
        );

        final long count = p.getTotalElements();
        final List<ConsumerOrderDto> records = p.stream().collect(Collectors.toList());
        final PageResultDto<ConsumerOrderDto> result = PageResultDto.of(pageIndex, pageSize, records, count);

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse confirmDelivery(UUID orderKey) {
        try {
            final OrderDeliveryCommand command = OrderDeliveryCommand.builder()
                .orderKey(orderKey)
                .consumerKey(this.currentUserKey())
                .build();

            this.orderFulfillmentService.receiveOrderByConsumer(command);

            return this.findOne(orderKey);
        } catch (final OrderException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse acceptContract(UUID orderKey) {
        try {
            final AcceptOrderContractCommand command = AcceptOrderContractCommand.builder()
                .orderKey(orderKey)
                .consumerKey(this.currentUserKey())
                .build();

            this.orderFulfillmentService.acceptContractByConsumer(command);

            return this.findOne(orderKey);
        } catch (final OrderException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadInvoice(UUID orderKey, HttpServletResponse response) {
        final ConsumerOrderDto order = this.orderRepository.findObjectByKeyAndConsumerAndStatusNotCreated(this.currentUserKey(), orderKey).orElse(null);

        if (order != null) {
            // Order reference number is equal to the PayIn reference number
            final Path path = this.invoiceFileManager.resolvePath(this.currentUserId(), order.getReferenceNumber());
            final File file = path.toFile();

            if (file.exists()) {
                return this.createDownloadResponsePdf(response, file, order.getReferenceNumber() + ".pdf");
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

}
