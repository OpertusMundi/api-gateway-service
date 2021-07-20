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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractDto;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.consumer.SignConsumerContractCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.service.contract.ConsumerContractService;
import eu.opertusmundi.common.service.contract.ContractFileManager;

@RestController
public class ConsumerContractControllerImpl extends BaseController implements ConsumerContractController {

    @Autowired
    private ContractFileManager fileManager;

    @Autowired
    private ConsumerContractService contractService;

    @Override
    public RestResponse<ConsumerContractDto> print(
        UUID orderKey, Integer itemIndex, HttpServletResponse response
    ) {
        try {
            final Path path = this.fileManager.resolvePath(this.currentUserId(), orderKey, itemIndex, false, false);

            final PrintConsumerContractCommand command = PrintConsumerContractCommand.builder()
                .userId(this.currentUserId())
                .orderKey(orderKey)
                .itemIndex(itemIndex)
                .path(path)
                .build();

            final ConsumerContractDto result = this.contractService.print(command);

            return RestResponse.result(result);
        } catch (final FileSystemException ex) {
            throw ex;
        }
    }

    @Override
    public RestResponse<ConsumerContractDto> sign(
        UUID orderKey, Integer itemIndex, HttpServletResponse response
    ) throws IOException {
        try {
            final Path sourcePath = this.fileManager.resolvePath(this.currentUserId(), orderKey, itemIndex, false, true);
            final Path targetPath = this.fileManager.resolvePath(this.currentUserId(), orderKey, itemIndex, true, false);

            final SignConsumerContractCommand command = SignConsumerContractCommand.builder()
                .userId(this.currentUserId())
                .orderKey(orderKey)
                .itemIndex(itemIndex)
                .sourcePath(sourcePath)
                .targetPath(targetPath)
                .build();

            final ConsumerContractDto result = this.contractService.sign(command);

            return RestResponse.result(result);
        } catch (final FileSystemException ex) {
            throw ex;
        }
    }

    @Override
    public ResponseEntity<StreamingResponseBody> download(
        UUID orderKey, Integer itemIndex, boolean signed, HttpServletResponse response
    ) {
        try {
            final Path path = this.fileManager.resolvePath(this.currentUserId(), orderKey, itemIndex, signed, true);
            final File file = path.toFile();

            if (!file.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found");
            }

            response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
            response.setHeader("Content-Type", MediaType.APPLICATION_PDF_VALUE);
            response.setHeader("Content-Length", Long.toString(file.length()));

            final StreamingResponseBody stream = out -> {
                try (InputStream inputStream = new FileInputStream(file)) {
                    IOUtils.copyLarge(inputStream, out);
                }
            };

            return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
        } catch (final FileSystemException ex) {
            throw ex;
        }
    }

}
