package eu.opertusmundi.web.feign.client;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.QueryResultPage;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.feign.client.config.MessageServiceFeignClientConfiguration;
import eu.opertusmundi.web.model.message.server.ServerMessageCommandDto;
import eu.opertusmundi.web.model.message.server.ServerMessageDto;
import eu.opertusmundi.web.model.message.server.ServerNotificationCommandDto;
import eu.opertusmundi.web.model.message.server.ServerNotificationDto;


@FeignClient(
    name = "${opertusmundi.feign.message-service.name}",
    url = "${opertusmundi.feign.message-service.url}",
    configuration = MessageServiceFeignClientConfiguration.class
)
public interface MessageServiceFeignClient {

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
     * @return An instance of {@link MessageEndPointTypes.MessageListResponseDto}
     */
    @GetMapping(value = "/v1/messages")
    ResponseEntity<RestResponse<QueryResultPage<ServerMessageDto>>> findMessages(
        @RequestParam(name = "page",      required = false) Integer       pageIndex,
        @RequestParam(name = "size",      required = false) Integer       pageSize,
        @RequestParam(name = "user",      required = true)  UUID          userKey,
        @RequestParam(name = "date-from", required = false) ZonedDateTime dateFrom,
        @RequestParam(name = "date-to",   required = false) ZonedDateTime dateTo,
        @RequestParam(name = "read",      required = false) Boolean       read
    );

    /**
     * Send message
     *
     * @param userKey Recipient user unique key
     * @param message Message configuration object
     *
     * @return An instance of {@link BaseResponse}
     */
    @PostMapping(value = "/v1/messages")
    ResponseEntity<BaseResponse> sendMessage(
        @RequestBody(required = true) ServerMessageCommandDto command
    );

    /**
     * Mark message as read
     *
     * @param key The message to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @PutMapping(value = "/v1/messages/user/{owner}/message/{key}")
    ResponseEntity<BaseResponse> readMessage(
        @PathVariable(name = "owner", required = true) UUID owner,
        @PathVariable(name = "key", required = true) UUID key
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
    @GetMapping(value = "/v1/notifications")
    ResponseEntity<RestResponse<QueryResultPage<ServerNotificationDto>>> findNotifications(
        @RequestParam(name = "page",      required = false) Integer       pageIndex,
        @RequestParam(name = "size",      required = false) Integer       pageSize,
        @RequestParam(name = "user",      required = true)  UUID          userKey,
        @RequestParam(name = "date-from", required = false) ZonedDateTime dateFrom,
        @RequestParam(name = "date-to",   required = false) ZonedDateTime dateTo,
        @RequestParam(name = "read",      required = false) Boolean       read
    );

    /**
     * Send notification
     *
     * @param notification Notification command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @PostMapping(value = "/v1/notifications")
    ResponseEntity<BaseResponse> sendNotification(
        @RequestBody(required = true) ServerNotificationCommandDto notification
    );

    /**
     * Mark notification as read
     *
     * @param key The key of the notification to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @PutMapping(value = "/v1/notifications/{key}")
    ResponseEntity<BaseResponse> readNotification(@PathVariable(name = "key", required = true) UUID key);

}
