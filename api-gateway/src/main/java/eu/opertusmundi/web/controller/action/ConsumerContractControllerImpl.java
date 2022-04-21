package eu.opertusmundi.web.controller.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.service.contract.ConsumerContractService;
import eu.opertusmundi.common.service.contract.ContractFileManager;

@RestController
public class ConsumerContractControllerImpl extends BaseController implements ConsumerContractController {

    @Autowired
    private ContractFileManager contractFileManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ConsumerContractService contractService;

    @Override
    public ResponseEntity<StreamingResponseBody> print(
        UUID orderKey, Integer itemIndex, HttpServletResponse response
    ) {
        final ConsumerOrderDto order = this.ensureOwner(orderKey);

        if (order.getItems().get(itemIndex - 1).getContractType() != EnumContractType.MASTER_CONTRACT) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        // Path will be resolved by the contract service
        final ConsumerContractCommand command = this.createCommand(orderKey, itemIndex);

        this.contractService.print(command);

        final File contractFile = command.getPath().toFile();

        return this.createResponse(response, contractFile, this.getFilename(order));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> sign(
        UUID orderKey, Integer itemIndex, HttpServletResponse response
    ) throws IOException {
        final OrderDto order = this.ensureOwner(orderKey);

        // Paths will be resolved by the contract service
        final ConsumerContractCommand command = this.createCommand(orderKey, itemIndex);

        this.contractService.sign(command);

        final File contractFile = command.getPath().toFile();

        return this.createResponse(response, contractFile, this.getFilename(order));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> download(
        UUID orderKey, Integer itemIndex, boolean signed, HttpServletResponse response
    ) {
        final ConsumerOrderDto order = this.ensureOwner(orderKey);
        final EnumContractType type  = order.getItems().get(itemIndex - 1).getContractType();

        Path path;
        File file;
        switch (type) {
            case MASTER_CONTRACT :
                path = this.contractFileManager.resolveMasterContractPath(this.currentUserId(), orderKey, itemIndex, signed);
                file = path.toFile();

                if (file.exists()) {
                    return this.createResponse(response, file, this.getFilename(order));
                }
                break;

            case UPLOADED_CONTRACT:
                path = this.contractFileManager.resolveUploadedContractPath(this.currentUserId(), orderKey, itemIndex, signed);
                file = path.toFile();

                if (file.exists()) {
                    return this.createResponse(response, file, this.getFilename(order));
                }
                break;
        }

        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<StreamingResponseBody> createResponse(
        HttpServletResponse response, File file, String downloadFilename
    ) {
        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", downloadFilename));
        response.setHeader("Content-Type", MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Length", Long.toString(file.length()));

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }

    private ConsumerOrderDto ensureOwner(UUID orderKey) {
        final ConsumerOrderDto order = this.orderRepository.findObjectByKeyAndConsumerKey(this.currentUserKey(), orderKey).orElse(null);

        if (order == null) {
            throw new AccessDeniedException("Access denied");
        }

        return order;
    }

    private ConsumerContractCommand createCommand(UUID orderKey, Integer itemIndex) {
        final ConsumerContractCommand command = ConsumerContractCommand.builder()
                .userId(this.currentUserId())
                .orderKey(orderKey)
                .itemIndex(itemIndex)
                .build();

        return command;
    }

    private String getFilename(OrderDto order) {
        return order.getReferenceNumber() + ".pdf";
    }

}
