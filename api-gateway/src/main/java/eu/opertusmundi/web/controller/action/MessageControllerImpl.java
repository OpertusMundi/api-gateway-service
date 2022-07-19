package eu.opertusmundi.web.controller.action;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.message.EnumMessageStatus;
import eu.opertusmundi.common.model.message.EnumNotificationSortField;
import eu.opertusmundi.common.model.message.client.ClientContactDto;
import eu.opertusmundi.common.model.message.client.ClientMessageCollectionResponse;
import eu.opertusmundi.common.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.common.model.message.client.ClientMessageDto;
import eu.opertusmundi.common.model.message.client.ClientMessageThreadResponse;
import eu.opertusmundi.common.model.message.client.ClientNotificationDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.service.messaging.MessageService;

@RestController
public class MessageControllerImpl extends BaseController implements MessageController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MessageService messageService;

    @Override
    public RestResponse<?> findMessages(Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageStatus status) {
        final PageResultDto<ClientMessageDto> messages = this.messageService.findMessages(
            this.currentUserKey(), pageIndex, pageSize, dateFrom, dateTo, status, null
        );
        final List<ClientContactDto>          contacts = this.messageService.findContacts(messages.getItems());
        final ClientMessageCollectionResponse result   = new ClientMessageCollectionResponse(messages, contacts);
        return result;
    }

    @Override
    public RestResponse<?> findNotifications(
        Integer pageIndex, Integer pageSize,
        ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read,
        EnumNotificationSortField orderBy, EnumSortingOrder order
    ) {
        final UUID userKey = this.currentUserKey();

        final PageResultDto<ClientNotificationDto> notifications = this.messageService.findNotifications(
            userKey, pageIndex, pageSize, dateFrom, dateTo, read, orderBy, order
        );

        return RestResponse.result(notifications);
    }

    @Override
    public RestResponse<?> sendToProvider(UUID providerKey, ClientMessageCommandDto clientCommand) {
        final AccountEntity provider = accountRepository.findOneByKey(providerKey).orElse(null);

        if (provider == null || !provider.hasRole(EnumRole.ROLE_PROVIDER)) {
            return RestResponse.error(BasicMessageCode.AccountNotFound, "Provider not found");
        }

        final ClientMessageDto result = this.messageService.sendMessage(this.currentUserKey(), providerKey, clientCommand);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> sendToHelpdesk(ClientMessageCommandDto clientMessage) {
        final ClientMessageDto result = this.messageService.sendMessage(this.currentUserKey(), null, clientMessage);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> replyToMessageThread(UUID threadKey, ClientMessageCommandDto clientMessage) {
        final ClientMessageDto result = this.messageService.replyToMessage(this.currentUserKey(), threadKey, clientMessage);

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse readMessage(UUID messageKey) {
        final ClientMessageDto result = this.messageService.readMessage(this.currentUserKey(), messageKey);

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse readThread(UUID threadKey) {
        final List<ClientMessageDto>      messages = this.messageService.readThread(this.currentUserKey(), threadKey);
        final List<ClientContactDto>      contacts = this.messageService.findContacts(messages);
        final ClientMessageThreadResponse result   = new ClientMessageThreadResponse(messages, contacts);

        return result;
    }

    @Override
    public RestResponse<?> getMessageThread(UUID threadKey) {
        final List<ClientMessageDto>      messages = this.messageService.getMessageThread(this.currentUserKey(), threadKey);
        final List<ClientContactDto>      contacts = this.messageService.findContacts(messages);
        final ClientMessageThreadResponse result   = new ClientMessageThreadResponse(messages, contacts);

        return result;
    }

    @Override
    public BaseResponse readNotification(UUID key) {
        this.messageService.readNotification(this.currentUserKey(), key);

        return RestResponse.success();
    }

    @Override
    public BaseResponse readAllNotifications() {
        this.messageService.readAllNotifications(this.currentUserKey());

        return RestResponse.success();
    }

}
