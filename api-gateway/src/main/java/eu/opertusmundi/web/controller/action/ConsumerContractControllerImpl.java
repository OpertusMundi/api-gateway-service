package eu.opertusmundi.web.controller.action;

import java.nio.file.Path;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.ConsumerOrderItemDto;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
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
    public ResponseEntity<StreamingResponseBody> downloadContract(
        UUID orderKey, Integer itemIndex, HttpServletResponse response
    ) {
        final ConsumerOrderDto     order  = this.ensureOwner(orderKey);
        final ConsumerOrderItemDto item   = order.getItems().get(itemIndex - 1);
        final EnumContractType     type   = item.getContractType();
        final boolean              signed = item.getContractSignedOn() != null;

        Path contractPath = null;

        switch (type) {
            case MASTER_CONTRACT :
                contractPath = this.resolveMasterContract(orderKey, itemIndex, signed);
                break;

            case UPLOADED_CONTRACT :
                // Provider must submit the contract before allowing consumer to
                // download it
                if (order.getStatus() != EnumOrderStatus.PENDING_PROVIDER_CONTRACT_UPLOAD) {
                    contractPath = this.resolveUploadedContract(orderKey, itemIndex, signed);
                }
                break;

            case OPEN_DATASET :
                // Not supported
                break;
        }
        if (contractPath == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return this.createDownloadResponsePdf(response, contractPath.toFile(), this.getFilename(order));
    }

    /**
     * Get the path of the contract of type `MASTER_CONTRACT` for a specific
     * order item
     *
     * @param orderKey
     * @param itemIndex
     * @param signed
     * @return
     */
    private Path resolveMasterContract(UUID orderKey, Integer itemIndex, boolean signed) {
        Path result = null;

        if (signed) {
            // Resolve the path of the signed contract
            result = this.contractFileManager.resolveMasterContractPath(this.currentUserId(), orderKey, itemIndex, true);
        } else {
            // Print contract draft
            final ConsumerContractCommand command = ConsumerContractCommand.builder()
                .userId(this.currentUserId())
                .orderKey(orderKey)
                .itemIndex(itemIndex)
                .build();

            this.contractService.print(command);

            result = command.getPath();
        }
        return result;
    }

    /**
     * Get the path of the contract of type `UPLOADED_CONTRACT` for a specific
     * order item
     *
     * @param orderKey
     * @param itemIndex
     * @param signed
     * @return
     */
    private Path resolveUploadedContract(UUID orderKey, Integer itemIndex, boolean signed) {
        final Path result = this.contractFileManager.resolveUploadedContractPath(this.currentUserId(), orderKey, itemIndex, signed);

        return result.toFile().exists() ? result : null;
    }

    private ConsumerOrderDto ensureOwner(UUID orderKey) {
        final ConsumerOrderDto order = this.orderRepository.findObjectByConsumerAndKey(this.currentUserKey(), orderKey).orElse(null);

        if (order == null) {
            throw new AccessDeniedException("Access denied");
        }

        return order;
    }

    private String getFilename(OrderDto order) {
        return order.getReferenceNumber() + ".pdf";
    }

}
