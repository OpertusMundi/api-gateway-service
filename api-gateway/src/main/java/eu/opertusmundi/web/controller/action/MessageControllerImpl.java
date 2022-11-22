package eu.opertusmundi.web.controller.action;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.message.ContactFormCommandDto;
import eu.opertusmundi.common.model.message.EnumMessageView;
import eu.opertusmundi.common.model.message.EnumNotificationSortField;
import eu.opertusmundi.common.model.message.client.ClientContactDto;
import eu.opertusmundi.common.model.message.client.ClientMessageCollectionResponse;
import eu.opertusmundi.common.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.common.model.message.client.ClientMessageDto;
import eu.opertusmundi.common.model.message.client.ClientMessageThreadDto;
import eu.opertusmundi.common.model.message.client.ClientMessageThreadResponse;
import eu.opertusmundi.common.model.message.client.ClientNotificationDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.service.messaging.ContactFormService;
import eu.opertusmundi.common.service.messaging.MessageService;

@RestController
public class MessageControllerImpl extends BaseController implements MessageController {

    private final AccountRepository  accountRepository;
    private final ContactFormService contactFormService;
    private final MessageService     messageService;

    @Autowired
    public MessageControllerImpl(
        AccountRepository     accountRepository,
        ContactFormService contactFormService,
        MessageService        messageService
    ) {
        this.accountRepository  = accountRepository;
        this.contactFormService = contactFormService;
        this.messageService     = messageService;
    }

    @Override
    public RestResponse<?> submitContactForm(ContactFormCommandDto command, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }
        final var result = this.contactFormService.create(command);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> findMessages(Integer pageIndex, Integer pageSize, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageView view) {
        final PageResultDto<ClientMessageDto> messages = this.messageService.findMessages(
            this.currentUserKey(), pageIndex, pageSize, dateFrom, dateTo, view, null
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
        final ClientMessageThreadDto      thread   = this.messageService.readThread(this.currentUserKey(), threadKey);
        final List<ClientContactDto>      contacts = this.messageService.findContacts(thread.getMessages());
        final ClientMessageThreadResponse result   = new ClientMessageThreadResponse(thread, contacts);

        return result;
    }

    @Override
    public RestResponse<?> getMessageThread(UUID threadKey) {
        final ClientMessageThreadDto thread = this.messageService.getMessageThread(this.currentUserKey(), threadKey);
        if (thread != null) {
            final List<ClientContactDto>      contacts = this.messageService.findContacts(thread.getMessages());
            final ClientMessageThreadResponse result   = new ClientMessageThreadResponse(thread, contacts);
            return result;
        }

        return RestResponse.notFound();
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
