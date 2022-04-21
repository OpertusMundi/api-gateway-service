package eu.opertusmundi.web.controller.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.order.EnumOrderSortField;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderConfirmCommandDto;
import eu.opertusmundi.common.model.order.OrderException;
import eu.opertusmundi.common.model.order.OrderShippingCommandDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.model.order.UploadOrderContractCommand;
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
        final Optional<ProviderOrderDto> r = this.orderRepository.findOrderObjectByKeyAndProvider(this.currentUserParentKey(), orderKey);
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
            this.currentUserParentKey(),
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
                this.orderFulfillmentService.rejectOrderByProvider(command);
            } else {
                this.orderFulfillmentService.acceptOrderByProvider(command);
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

    @Override
    public BaseResponse uploadOrderContract(UUID orderKey, MultipartFile contract) {
        return this.uploadOrderContract(orderKey, contract, false);
    }

    @Override
    public BaseResponse uploadOrderContractAndSubmit(UUID orderKey, MultipartFile contract) {
        return this.uploadOrderContract(orderKey, contract, true);
    }

    private BaseResponse uploadOrderContract(UUID orderKey, MultipartFile contract, boolean lastUpdate) {
        try {
            final UploadOrderContractCommand command = UploadOrderContractCommand.builder()
                .providerKey(this.currentUserParentKey())
                .orderKey(orderKey)
                .itemIndex(1)
                .size(contract.getSize())
                .fileName(contract.getOriginalFilename())
                .lastUpdate(lastUpdate)
                .build();

            final InputStream inputStream = new ByteArrayInputStream(contract.getBytes());
            this.orderFulfillmentService.uploadContractByProvider(command, inputStream);

            return this.findOne(orderKey);
        }  catch (final ServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error(String.format("Failed to upload file. [orderKey=%s]", orderKey), ex);
        }

        return RestResponse.failure();
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadContract(
        UUID orderKey, HttpServletResponse response
    ) throws IOException {
        final UUID providerKey = this.currentUserParentKey();
        final Path path        = this.orderFulfillmentService.resolveOrderContractPath(providerKey, orderKey);

        if (path == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        final File file        = path.toFile();
        String     contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
        response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Length", Long.toString(file.length()));

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<>(stream, HttpStatus.OK);
    }

}
