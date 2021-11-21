package eu.opertusmundi.web.controller.action;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.message.EnumNotificationSortField;
import eu.opertusmundi.common.model.message.client.ClientMessageCollectionResponse;
import eu.opertusmundi.common.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.common.model.message.client.ClientMessageThreadResponse;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.MessageEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


/**
 * Endpoint for user messages and notifications
 */
@RequestMapping(path = "/action", produces = MediaType.APPLICATION_JSON_VALUE)
public interface MessageController {

    /**
     * Find messages
     *
     * @param pageIndex
     * @param pageSize
     * @param dateFrom
     * @param dateTo
     * @param read
     * @return
     */
    @Operation(
        operationId = "message-01",
        summary     = "Find messages",
        description = "Find the messages of the authenticated user. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.Message }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ClientMessageCollectionResponse.class)
        )
    )
    @GetMapping(value = "/messages")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    RestResponse<?> findMessages(
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page index",
            schema      = @Schema(type = "integer", defaultValue = "0")
        )
        @RequestParam(name = "page", required = false) Integer pageIndex,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page size",
            schema      = @Schema(type = "integer", defaultValue = "10")
        )
        @RequestParam(name = "size", required = false) Integer pageSize,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter messages after date"
        )
        @RequestParam(name = "date-from", required = false) ZonedDateTime dateFrom,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter messages before date"
        )
        @RequestParam(name = "date-to", required = false) ZonedDateTime dateTo,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter read messages"
        )
        @RequestParam(name = "read", required = false) Boolean read
    );

    /**
     * Find notifications
     *
     * @param pageIndex
     * @param pageSize
     * @param dateFrom
     * @param dateTo
     * @param read
     * @return
     */
    @Operation(
        operationId = "notification-01",
        summary     = "Find notifications",
        description = "Find the notifications of the authenticated user. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.Notification }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageEndpointTypes.NotificationListResponseDto.class)
        )
    )
    @GetMapping(value = "/notifications")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    RestResponse<?> findNotifications(
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page index",
            schema      = @Schema(type = "integer", defaultValue = "0")
        )
        @RequestParam(name = "page", required = false) Integer pageIndex,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Page size",
            schema      = @Schema(type = "integer", defaultValue = "10")
        )
        @RequestParam(name = "size", required = false) Integer pageSize,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter notifications after date"
        )
        @RequestParam(name = "date-from", required = false) ZonedDateTime dateFrom,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter notifications before date"
        )
        @RequestParam(name = "date-to", required = false) ZonedDateTime dateTo,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Filter read notifications"
        )
        @RequestParam(name = "read", required = false) Boolean read,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order by property"
        )
        @RequestParam(name = "orderBy", defaultValue = "SEND_AT") EnumNotificationSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "DESC") EnumSortingOrder order
    );

    /**
     * Send a message to the provider with the specified key
     *
     * @param providerKey Recipient provider unique key
     * @param command Message command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "message-02",
        summary     = "Send a message to a provider",
        description = "Sends a message to the specified provider from the authenticated user. Required role: `ROLE_CONSUMER`",
        tags        = { EndpointTags.Message }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageEndpointTypes.MessageResponseDto.class))
    )
    @PostMapping(value = "/messages/provider/{providerKey}")
    @Secured({"ROLE_CONSUMER"})
    RestResponse<?> sendToProvider(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Provider unique id"
        )
        @PathVariable(name = "providerKey", required = true) UUID providerKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Message",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClientMessageCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) ClientMessageCommandDto command
    );

    /**
     * Send message to Helpdesk
     *
     * @param command Message command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "message-03",
        summary     = "Send a message to Helpdesk",
        description = "Sends a message to Helpdesk from the authenticated user. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.Message }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageEndpointTypes.MessageResponseDto.class))
    )
    @PostMapping(value = "/messages/helpdesk")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    RestResponse<?> sendToHelpdesk(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Message",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClientMessageCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) ClientMessageCommandDto command
    );

    /**
     * Reply to message
     *
     * @param threadKey Reply to message thread with the specified key
     * @param command Message command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "message-04",
        summary     = "Reply to a message",
        description = "Reply to a message thread that belongs to the authenticated user. "
                    + "Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.Message }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageEndpointTypes.MessageResponseDto.class))
    )
    @PostMapping(value = "/messages/thread/{threadKey}")
    @Secured({"ROLE_CONSUMER", "ROLE_PROVIDER", "ROLE_VENDOR_USER"})
    RestResponse<?> replyToMessageThread(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message thread unique key"
        )
        @PathVariable(name = "threadKey", required = true) UUID threadKey,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Message",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClientMessageCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) ClientMessageCommandDto command
    );

    /**
     * Mark message as read
     *
     * @param messageKey The message to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "message-05",
        summary     = "Read message",
        description = "Marks a message as read. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.Message }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/messages/{messageKey}")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    BaseResponse readMessage(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message unique key"
        )
        @PathVariable(name = "messageKey", required = true) UUID messageKey
    );

    /**
     * Get all thread messages
     *
     * @param threadKey The key of any message thread
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "message-06",
        summary     = "Get message thread",
        description = "Get all messages of a thread. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.Message }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClientMessageThreadResponse.class))
    )
    @GetMapping(value = "/messages/thread/{threadKey}")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    RestResponse<?> getMessageThread(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message thread unique key"
        )
        @PathVariable(name = "threadKey", required = true) UUID threadKey
    );

    /**
     * Mark notification as read
     *
     * @param key The notification to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "notification-02",
        summary     = "Read notification",
        description = "Marks a notification as read. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.Notification }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/notifications/{key}")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    BaseResponse readNotification(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Notification unique key"
        )
        @PathVariable(name = "key", required = true) UUID key
    );

    /**
     * Mark all notifications as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "notification-03",
        summary     = "Read all notifications",
        description = "Marks all notifications as read. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.Notification }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/notifications")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    BaseResponse readAllNotifications();

}
