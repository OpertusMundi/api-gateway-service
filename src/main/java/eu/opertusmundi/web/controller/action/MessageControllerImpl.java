package eu.opertusmundi.web.controller.action;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import brave.Span;
import brave.Tracer;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.QueryResultPage;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.feign.client.MessageServiceFeignClient;
import eu.opertusmundi.web.model.message.client.ClientMessageCollectionResponse;
import eu.opertusmundi.web.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.web.model.message.client.ClientMessageDto;
import eu.opertusmundi.web.model.message.client.ClientNotificationDto;
import eu.opertusmundi.web.model.message.client.ClientRecipientDto;
import eu.opertusmundi.web.model.message.server.ServerBaseMessageCommandDto;
import eu.opertusmundi.web.model.message.server.ServerMessageCommandDto;
import eu.opertusmundi.web.model.message.server.ServerMessageDto;
import eu.opertusmundi.web.model.message.server.ServerNotificationCommandDto;
import eu.opertusmundi.web.model.message.server.ServerNotificationDto;
import eu.opertusmundi.web.repository.AccountRepository;
import feign.FeignException;

@RestController
public class MessageControllerImpl extends BaseController implements MessageController {

    @Autowired
    Tracer tracer;

    @Autowired
    private ObjectProvider<MessageServiceFeignClient> messageClient;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public RestResponse<?> findMessages(Integer pageIndex, Integer pageSize, UUID userKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read) {
        ResponseEntity<RestResponse<QueryResultPage<ServerMessageDto>>> e;

        // Override user key
        if (userKey == null || !this.authenticationFacade.hasRole(EnumRole.ROLE_HELPDESK)) {
            userKey = this.currentUserKey();
        }

        // Get messages
        try {
            e = this.messageClient.getObject().findMessages(pageIndex, pageSize, userKey, dateFrom, dateTo, read);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }

        final RestResponse<QueryResultPage<ServerMessageDto>> serviceResponse = e.getBody();

        if(!serviceResponse.getSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        // Get all recipients in the result
        final Span span = this.tracer.nextSpan().name("database-recipient").start();

        final List<ClientRecipientDto> recipients;

        try {
            final UUID[] recipientKeys = serviceResponse.getResult().getItems().stream().map(i -> i.getRecipient()).distinct().toArray(UUID[]::new);

            recipients = this.accountRepository.findAllByKey(recipientKeys).stream()
                .map(ClientRecipientDto::new)
                .collect(Collectors.toList());

            // TODO: Check that all recipients exists
            Assert.isTrue(recipientKeys.length == recipients.size(), "All recipients must exist!");
        } catch(final Exception ex) {
            // TODO: Add logging
            return RestResponse.failure();
        } finally {
            span.finish();
        }

        // Compose response and send
        final QueryResultPage<ClientMessageDto> serviceResult = serviceResponse.getResult().convert(m -> {
            return new ClientMessageDto(m);
        });

        final ClientMessageCollectionResponse result = new ClientMessageCollectionResponse(serviceResult, recipients);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findNotifications(Integer pageIndex, Integer pageSize, UUID userKey, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read) {
        ResponseEntity<RestResponse<QueryResultPage<ServerNotificationDto>>> e;

        try {
            e = this.messageClient.getObject().findNotifications(pageIndex, pageSize, userKey, dateFrom, dateTo, read);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }

        final RestResponse<QueryResultPage<ServerNotificationDto>> serviceResponse = e.getBody();

        if(!serviceResponse.getSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        final QueryResultPage<ClientNotificationDto> result = serviceResponse.getResult().convert(n -> {
            return new ClientNotificationDto(n);
        });

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse sendToUser(UUID key, ClientMessageCommandDto message) {
        final ServerMessageCommandDto command = new ServerMessageCommandDto();

        command.setSender(this.authenticationFacade.getCurrentUserKey());
        command.setRecipient(key);
        command.setText(message.getText());

        final BaseResponse serviceResponse = this.send(command);

        final BaseResponse response = this.processSendResponse(serviceResponse);

        return response;
    }

    @Override
    public BaseResponse sendToProvider(UUID key, ClientMessageCommandDto message) {
        final ServerMessageCommandDto command = new ServerMessageCommandDto();

        command.setSender(this.authenticationFacade.getCurrentUserKey());
        command.setRecipient(key);
        command.setText(message.getText());

        final BaseResponse serviceResponse = this.send(command);

        final BaseResponse response = this.processSendResponse(serviceResponse);

        return response;
    }

    @Override
    public BaseResponse sendToHelpdesk(ClientMessageCommandDto message) {
        final ServerMessageCommandDto command = new ServerMessageCommandDto();

        command.setSender(this.authenticationFacade.getCurrentUserKey());
        command.setText(message.getText());

        final BaseResponse serviceResponse = this.send(command);

        final BaseResponse response = this.processSendResponse(serviceResponse);

        return response;
    }

    @Override
    public BaseResponse replyToMessage(UUID key, ClientMessageCommandDto message) {
        final ServerMessageCommandDto command = new ServerMessageCommandDto();

        command.setSender(this.authenticationFacade.getCurrentUserKey());
        command.setMessage(key);
        command.setText(message.getText());

        final BaseResponse serviceResponse = this.send(command);

        final BaseResponse response = this.processSendResponse(serviceResponse);

        return response;
    }

    @Override
    public BaseResponse readMessage(UUID key) {
        ResponseEntity<BaseResponse> e;

        try {
            e = this.messageClient.getObject().readMessage(this.currentUserKey(), key);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }

        final BaseResponse serviceResponse = e.getBody();

        if(!serviceResponse.getSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        // TODO: Format response ...
        return serviceResponse;
    }

    @Override
    public BaseResponse readNotification(UUID key) {
        ResponseEntity<BaseResponse> e;

        try {
            e = this.messageClient.getObject().readNotification(key);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }

        final BaseResponse serviceResponse = e.getBody();

        if(!serviceResponse.getSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        // TODO: Format response ...
        return serviceResponse;
    }

    private BaseResponse send(ServerBaseMessageCommandDto command) throws IllegalArgumentException, FeignException {
        ResponseEntity<BaseResponse> e;

        switch (command.getType()) {
            case MESSAGE :
                final ServerMessageCommandDto message = (ServerMessageCommandDto) command;

                e = this.messageClient.getObject().sendMessage(message);
                break;

            case NOTIFICATION :
                final ServerNotificationCommandDto notification = (ServerNotificationCommandDto) command;

                e = this.messageClient.getObject().sendNotification(notification);
                break;

            default :
                throw new IllegalArgumentException("Message type is not supported");
        }

        return e.getBody();
    }

    private BaseResponse processSendResponse(BaseResponse response) {
        if (!response.getSuccess()) {
            // TODO: Add logging ...
            return RestResponse.failure();
        }

        // TODO: Format response ...
        return response;
    }

}
