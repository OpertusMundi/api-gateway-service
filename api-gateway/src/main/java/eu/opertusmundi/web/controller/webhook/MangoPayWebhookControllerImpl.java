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

import eu.opertusmundi.common.service.MangoPayWebhookHandler;
import eu.opertusmundi.common.service.PaymentService;

@Controller
public class MangoPayWebhookControllerImpl implements MangoPayWebhookController {

    private static final Logger logger = LoggerFactory.getLogger("WEBHOOK");

    @Autowired
    private MangoPayWebhookHandler handler;

    @Autowired
    private PaymentService paymentService;

    @Override
    public ResponseEntity<Void> mangoPayWebhookHandler(String resourceId, Long timestamp, String eventType) {
        logger.info(String.format("%30s %30s %30s", eventType, resourceId, timestamp));

        final ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);

        this.handler.handleWebHook(eventType, resourceId, date);

        // Webhook received successfully
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @Override
    public String secureModeRedirectHandler(UUID payInKey, String transactionId) {
        // TODO: Reset cart if PayIn is successful

        this.paymentService.sendPayInStatusUpdateMessage(payInKey, transactionId);

        return "index";
    }

}
