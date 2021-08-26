package eu.opertusmundi.web.controller.action;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.feign.client.MessageServiceFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.message.client.ClientContactDto;
import eu.opertusmundi.common.model.message.client.ClientMessageCollectionResponse;
import eu.opertusmundi.common.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.common.model.message.client.ClientMessageDto;
import eu.opertusmundi.common.model.message.client.ClientMessageThreadResponse;
import eu.opertusmundi.common.model.message.client.ClientNotificationDto;
import eu.opertusmundi.common.model.message.server.ServerBaseMessageCommandDto;
import eu.opertusmundi.common.model.message.server.ServerMessageCommandDto;
import eu.opertusmundi.common.model.message.server.ServerMessageDto;
import eu.opertusmundi.common.model.message.server.ServerNotificationDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.HelpdeskAccountRepository;
import feign.FeignException;

@RestController
public class MessageControllerImpl extends BaseController implements MessageController {

    @Autowired
    private ObjectProvider<MessageServiceFeignClient> messageClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HelpdeskAccountRepository helpdeskAccountRepository;

    @Override
    public RestResponse<?> findMessages(Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read) {
        try {
            final ResponseEntity<RestResponse<PageResultDto<ServerMessageDto>>> e = this.messageClient.getObject()
                .findMessages(this.currentUserKey(), pageIndex, pageSize, dateFrom, dateTo, read);

            final RestResponse<PageResultDto<ServerMessageDto>> serviceResponse = e.getBody();

            if(!serviceResponse.getSuccess()) {
                // TODO: Add logging ...
                return RestResponse.failure();
            }

            final List<ClientContactDto> contracts = this.getContacts(serviceResponse.getResult().getItems());

            final PageResultDto<ClientMessageDto> serviceResult = serviceResponse.getResult().convert(ClientMessageDto::from);

            final ClientMessageCollectionResponse result = new ClientMessageCollectionResponse(serviceResult, contracts);

            return result;
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

    @Override
    public RestResponse<?> findNotifications(Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read) {
        try {
            final UUID userKey = this.currentUserKey();

            final ResponseEntity<RestResponse<PageResultDto<ServerNotificationDto>>> e = this.messageClient.getObject()
                .findNotifications(pageIndex, pageSize, userKey, dateFrom, dateTo, read);

            final RestResponse<PageResultDto<ServerNotificationDto>> serviceResponse = e.getBody();

            if(!serviceResponse.getSuccess()) {
                // TODO: Add logging ...
                return RestResponse.failure();
            }

            final PageResultDto<ClientNotificationDto> result = serviceResponse.getResult().convert(n -> {
                return new ClientNotificationDto(n);
            });

            return RestResponse.result(result);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

    @Override
    public RestResponse<?> sendToProvider(UUID providerKey, ClientMessageCommandDto clientCommand) {
        try {
            final AccountEntity provider = accountRepository.findOneByKey(providerKey).orElse(null);
            if (provider == null || !provider.hasRole(EnumRole.ROLE_PROVIDER)) {
                return RestResponse.error(BasicMessageCode.AccountNotFound, "Provider not found");
            }

            final ServerMessageCommandDto serverCommand = new ServerMessageCommandDto();

            serverCommand.setSender(this.currentUserKey());
            serverCommand.setRecipient(providerKey);
            serverCommand.setText(clientCommand.getText());

            final RestResponse<ServerMessageDto> serviceResponse = this.send(serverCommand);

            return this.processSendResponse(serviceResponse);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

    @Override
    public RestResponse<?> sendToHelpdesk(ClientMessageCommandDto clientMessage) {
        try {
            final ServerMessageCommandDto serverMessage = new ServerMessageCommandDto();

            serverMessage.setSender(this.currentUserKey());
            serverMessage.setText(clientMessage.getText());

            final RestResponse<ServerMessageDto> serviceResponse = this.send(serverMessage);

            return this.processSendResponse(serviceResponse);
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

    @Override
    public RestResponse<?> replyToMessageThread(UUID threadKey, ClientMessageCommandDto clientMessage) {
            try {
                final ServerMessageCommandDto command = new ServerMessageCommandDto();

                command.setSender(this.currentUserKey());
                command.setThread(threadKey);
                command.setText(clientMessage.getText());

                final RestResponse<ServerMessageDto> serviceResponse = this.send(command);

                return this.processSendResponse(serviceResponse);
            } catch (final FeignException fex) {
                final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

                // TODO: Add logging ...

                return RestResponse.error(code, "An error has occurred");
            }
    }

    @Override
    public BaseResponse readMessage(UUID messageKey) {
        try {
            final ResponseEntity<RestResponse<ServerMessageDto>> e = this.messageClient.getObject().readMessage(this.currentUserKey(), messageKey);

            final RestResponse<ServerMessageDto> serviceResponse = e.getBody();

            if (serviceResponse.getSuccess()) {
                return RestResponse.result(ClientMessageDto.from(serviceResponse.getResult()));
            }

            return RestResponse.failure();
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

    @Override
    public RestResponse<?> getMessageThread(UUID threadKey) {
        try {
            final ResponseEntity<RestResponse<List<ServerMessageDto>>> e = this.messageClient.getObject()
                .getMessageThread(threadKey, this.currentUserKey());

            final RestResponse<List<ServerMessageDto>> serviceResponse = e.getBody();

            if(!serviceResponse.getSuccess()) {
                return RestResponse.failure();
            }

            final List<ClientMessageDto> messages = serviceResponse.getResult().stream()
               .map(ClientMessageDto::from)
               .collect(Collectors.toList());


            final List<ClientContactDto>      contracts = this.getContacts(serviceResponse.getResult());
            final ClientMessageThreadResponse result    = new ClientMessageThreadResponse(messages, contracts);

            return result;
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

    @Override
    public BaseResponse readNotification(UUID key) {
        try {
            final ResponseEntity<BaseResponse> e = this.messageClient.getObject().readNotification(key);

            final BaseResponse serviceResponse = e.getBody();

            if(!serviceResponse.getSuccess()) {
                // TODO: Add logging ...
                return RestResponse.failure();
            }

            // TODO: Format response ...
            return serviceResponse;
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...

            return RestResponse.error(code, "An error has occurred");
        }
    }

    private RestResponse<ServerMessageDto> send(ServerBaseMessageCommandDto command) throws IllegalArgumentException, FeignException {
        switch (command.getType()) {
            case MESSAGE :
                final ServerMessageCommandDto message = (ServerMessageCommandDto) command;
                final ResponseEntity<RestResponse<ServerMessageDto>> e = this.messageClient.getObject().sendMessage(message);

                return e.getBody();

            default :
                throw new IllegalArgumentException("Message type is not supported");
        }
    }

    private RestResponse<?> processSendResponse(RestResponse<ServerMessageDto> response) {
        if (response.getSuccess()) {
            final ClientMessageDto result = ClientMessageDto.from(response.getResult());
            return RestResponse.result(result);
        }

        return RestResponse.failure();
    }


    private List<ClientContactDto> getContacts(List<ServerMessageDto> messages) {
        final List<ClientContactDto> contacts     = new ArrayList<>();
        final List<UUID>             contractKeys = new ArrayList<>();

        messages.stream()
            .map(i -> i.getSender())
            .forEach(contractKeys::add);

        messages.stream()
            .map(i -> i.getRecipient())
            .forEach(contractKeys::add);

        final List<UUID> uniqueContractKeys = contractKeys.stream().filter(k -> k != null).distinct().collect(Collectors.toList());

        this.accountRepository.findAllByKey(uniqueContractKeys).stream()
            .map(ClientContactDto::new)
            .forEach(contacts::add);

        this.helpdeskAccountRepository.findAllByKey(uniqueContractKeys).stream()
            .map(ClientContactDto::new)
            .forEach(contacts::add);

        return contacts;
    }


}
