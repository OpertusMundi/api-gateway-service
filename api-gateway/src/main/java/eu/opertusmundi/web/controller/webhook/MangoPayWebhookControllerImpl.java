package eu.opertusmundi.web.controller.webhook;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.MessageCode;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.service.mangopay.MangoPayWebhookHandler;
import eu.opertusmundi.common.service.mangopay.PaymentService;

@Controller
public class MangoPayWebhookControllerImpl implements MangoPayWebhookController {

    private static final Logger logger = LoggerFactory.getLogger("WEBHOOK");

    @Autowired
    private MangoPayWebhookHandler handler;

    @Autowired
    private PaymentService paymentService;

    @Override
    public ResponseEntity<Void> mangoPayWebhookHandler(String resourceId, Long timestamp, String eventType) {
        HttpStatus  status = HttpStatus.INTERNAL_SERVER_ERROR;
        MessageCode code   = BasicMessageCode.InternalServerError;

        try {
            final ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);

            this.handler.handleWebHook(eventType, resourceId, date);

            status = HttpStatus.OK;
            code   = BasicMessageCode.Success;
        } catch (final Exception ex) {
            // Ignore errors
            if (ex instanceof PaymentException) {
                final PaymentException pEx = (PaymentException) ex;
                // Ignore unregistered web hooks
                if (pEx.getCode() == PaymentMessageCode.WEB_HOOK_NOT_SUPPORTED) {
                    status = HttpStatus.OK;
                }
            }
            if (ex instanceof ServiceException) {
                code = ((ServiceException) ex).getCode();
            }
        }

        logger.info(String.format("%30s %30s %30s %d %s", eventType, resourceId, timestamp, status.value(), code.key()));
        return new ResponseEntity<Void>(status);
    }

    @Override
    public String secureModeRedirectHandler(UUID payInKey, String transactionId) {
        // TODO: Reset cart if PayIn is successful

        this.paymentService.sendPayInStatusUpdateMessage(payInKey, transactionId);

        return "index";
    }

}
