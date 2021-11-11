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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.contract.EnumContract;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.consumer.SignConsumerContractCommand;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.service.contract.ConsumerContractService;
import eu.opertusmundi.common.service.contract.ContractFileManager;

@RestController
public class ConsumerContractControllerImpl extends BaseController implements ConsumerContractController {

    @Autowired
    private ContractFileManager fileManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ConsumerContractService contractService;

    @Override
    public ResponseEntity<StreamingResponseBody> print(
        UUID orderKey, Integer itemIndex, HttpServletResponse response
    ) {
        final OrderDto order = this.ensureOwner(orderKey);

        // Path will be resolved by the contract service
        final PrintConsumerContractCommand command = PrintConsumerContractCommand.builder()
            .userId(this.currentUserId())
            .orderKey(orderKey)
            .itemIndex(itemIndex)
            .type(EnumContract.USER_CONTRACT)
            .build();

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
        final SignConsumerContractCommand command = SignConsumerContractCommand.builder()
            .userId(this.currentUserId())
            .orderKey(orderKey)
            .itemIndex(itemIndex)
            .build();

        this.contractService.sign(command);

        final File contractFile = command.getTargetPath().toFile();

        return this.createResponse(response, contractFile, this.getFilename(order));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> download(
        UUID orderKey, Integer itemIndex, boolean signed, HttpServletResponse response
    ) {
        final OrderDto order = this.ensureOwner(orderKey);

        final Path path = this.fileManager.resolvePath(this.currentUserId(), orderKey, itemIndex, signed, true);
        final File file = path.toFile();

        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found");
        }

        return this.createResponse(response, file, this.getFilename(order));
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

    private OrderDto ensureOwner(UUID orderKey) {
        final OrderDto order = this.orderRepository.findObjectByKeyAndConsumerKey(this.currentUserKey(), orderKey).orElse(null);

        if (order == null) {
            throw new AccessDeniedException("Access denied");
        }

        return order;
    }

    private String getFilename(OrderDto order) {
        return order.getReferenceNumber() + ".pdf";
    }

}
