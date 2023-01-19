package eu.opertusmundi.web.controller.webhook;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.MessageCode;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.web.controller.action.BaseController;
import eu.opertusmundi.web.model.AccountMapCommand;
import eu.opertusmundi.web.model.EnumTopioMapsEvent;
import eu.opertusmundi.web.repository.AccountMapRepository;

@Controller
public class TopioMapsWebhookControllerImpl extends BaseController implements TopioMapsWebhookController {

    private static final Logger logger = LoggerFactory.getLogger("WEBHOOK-TOPIO-MAPS");

    private final AccountMapRepository accountMapRepository;

    @Autowired
    public TopioMapsWebhookControllerImpl(AccountMapRepository accountMapRepository) {
        this.accountMapRepository = accountMapRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<Void> webhookHandler(Long timestamp, String eventType, ObjectNode attributes) {
        HttpStatus  status = HttpStatus.INTERNAL_SERVER_ERROR;
        MessageCode code   = BasicMessageCode.InternalServerError;

        try {
            final var date  = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
            final var event = EnumTopioMapsEvent.from(eventType);

            if (event == null) {
                status = HttpStatus.BAD_REQUEST;
                throw new ServiceException(BasicMessageCode.BadRequest, "Event type not supported");
            }

            this.handleEvent(date, event, attributes);

            status = HttpStatus.OK;
            code   = BasicMessageCode.Success;
        } catch (final Exception ex) {
            if (ex instanceof final ServiceException serviceEx) {
                code = serviceEx.getCode();
            }
        }

        logger.info(String.format("%30s %30s %d %s", eventType, timestamp.toString(), status.value(), code.key()));
        return new ResponseEntity<Void>(status);
    }

    private void handleEvent(ZonedDateTime date, EnumTopioMapsEvent event, ObjectNode attributes) {
        switch (event) {
            case MAP_CREATED :
                final var command = AccountMapCommand.builder()
                    .attributes(attributes)
                    .createdAt(date)
                    .mapUrl(attributes.get("url").asText())
                    .thumbnailUrl(attributes.get("thumbnail").isNull() ? null : attributes.get("thumbnail").asText())
                    .title(attributes.get("title").asText())
                    .userKey(this.currentUserKey())
                    .build();

                this.accountMapRepository.create(command);
                break;
            case MAP_DELETED :
                final var map = this.accountMapRepository
                    .findOneByUrl(this.currentUserKey(), attributes.get("url").asText())
                    .orElse(null);
                if (map != null) {
                    this.accountMapRepository.delete(map);
                }
                break;

        }
    }

}
