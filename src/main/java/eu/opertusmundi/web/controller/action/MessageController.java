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
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.message.client.ClientMessageCollectionResponse;
import eu.opertusmundi.web.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.web.model.openapi.schema.MessageEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Endpoint messages and notifications
 */
@Tag(
    name        = "Message",
    description = "The message and notification API"
)
@RequestMapping(path = "/action", produces = MediaType.APPLICATION_JSON_VALUE)
public interface MessageController {

    /**
     * Find messages
     *
     * @param pageIndex
     * @param pageSize
     * @param userKey
     * @param dateFrom
     * @param dateTo
     * @param read
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Find messages",
        tags        = { "Message" }
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
    @Secured({"ROLE_USER", "ROLE_HELPDESK"})
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
            description = "Filter user by key"
        )
        @RequestParam(name = "user", required = false) UUID userKey,
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
     * @param userKey
     * @param dateFrom
     * @param dateTo
     * @param read
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Find notifications",
        tags        = { "Notification" }
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
    @Secured({"ROLE_USER"})
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
            description = "Filter user by key"
        )
        @RequestParam(name = "user", required = false) UUID userKey,
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
        @RequestParam(name = "read", required = false) Boolean read
    );

    /**
     * Send a message to the platform user with the specified key
     *
     * @param key Recipient user unique key
     * @param message Message command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Send a message to a platform user",
        description = "Sends a message to the specified user from a Helpdesk account",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageEndpointTypes.MessageReceiptDto.class)
        )
    )
    @PostMapping(value = "/message/user/{key}")
    @Secured({"ROLE_HELPDESK"})
    BaseResponse sendToUser(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "User unique key"
        )
        @PathVariable(name = "key", required = true) UUID key,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Message",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ClientMessageCommandDto.class)
            ),
            required = true
        )
        @RequestBody(required = true) ClientMessageCommandDto message
    );

    /**
     * Send a message to the provider with the specified key
     *
     * @param key Recipient provider unique key
     * @param message Message command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Send a message to a provider",
        description = "Sends a message to the specified provider from the authenticated user",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageEndpointTypes.MessageReceiptDto.class)
        )
    )
    @PostMapping(value = "/message/provider/{key}")
    @Secured({"ROLE_USER", "ROLE_HELPDESK"})
    BaseResponse sendToProvider(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Provider unique id"
        )
        @PathVariable(name = "key", required = true) UUID key,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Message",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClientMessageCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) ClientMessageCommandDto message
    );

    /**
     * Send message to Helpdesk
     *
     * @param message Message configuration object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Send a message to Helpdesk",
        description = "Sends a message to Helpdesk from the authenticated user",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageEndpointTypes.MessageReceiptDto.class))
    )
    @PostMapping(value = "/message/helpdesk")
    @Secured({"ROLE_USER", "ROLE_PROVIDER"})
    BaseResponse sendToHelpdesk(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Message",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClientMessageCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) ClientMessageCommandDto message
    );

    /**
     * Reply to message
     *
     * @param key Reply to message with the specified key
     * @param message Message command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Reply to a message",
        description = "Reply to a message accessible to the authenticated user",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageEndpointTypes.MessageReceiptDto.class))
    )
    @PostMapping(value = "/message/{key}")
    @Secured({"ROLE_USER", "ROLE_PROVIDER", "ROLE_HELPDESK"})
    BaseResponse replyToMessage(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message unique key"
        )
        @PathVariable(name = "key", required = true) UUID key,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Message",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClientMessageCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) ClientMessageCommandDto message
    );

    /**
     * Mark message as read
     *
     * @param key The message to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Read message",
        description = "Marks a message as read",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/message/{key}")
    @Secured({"ROLE_USER", "ROLE_PROVIDER", "ROLE_HELPDESK"})
    BaseResponse readMessage(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Message unique key"
        )
        @PathVariable(name = "key", required = true) UUID key
    );

    /**
     * Mark notification as read
     *
     * @param key The notification to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        summary     = "Read notification",
        description = "Marks a notification as read",
        tags        = { "Message" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))
    )
    @PutMapping(value = "/notification/{key}")
    @Secured({"ROLE_USER", "ROLE_PROVIDER", "ROLE_HELPDESK"})
    BaseResponse readNotification(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Notification unique key"
        )
        @PathVariable(name = "key", required = true) UUID key
    );

}
