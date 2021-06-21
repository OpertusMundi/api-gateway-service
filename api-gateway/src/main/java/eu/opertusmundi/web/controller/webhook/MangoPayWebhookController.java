package eu.opertusmundi.web.controller.webhook;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Hidden
public interface MangoPayWebhookController {

    /**
     * Handles MANGOPAY webhook events
     *
     * For details on method performance requirements see MANGOPAY documentation.
     *
     * @see <a href="https://docs.mangopay.com/endpoints/v2.01/hooks#e246_the-hook-object">The Hook object</a>
     *
     * @param resourceId
     * @param timestamp
     * @param eventType
     * @return
     */
    @Operation(
        operationId = "webhook-01",
        summary     = "MANGOPAY",
        description = "Handles MANGOPAY webhook events"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation"
    )
    @GetMapping(value = "/webhooks/mangopay")
    ResponseEntity<Void> mangoPayWebhookHandler(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "The ID of whatever the event is"
        )
        @RequestParam(name = "RessourceId", required = true) String resourceId,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "When the event happened"
        )
        @RequestParam(name = "Date", required = true) Long timestamp,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "The event type"
        )
        @RequestParam(name = "EventType", required = true) String eventType
    );

    /**
     * Handles 3-D Secure validation redirects
     *
     * @see <a href="https://docs.mangopay.com/endpoints/v2.01/payins#e278_create-a-card-direct-payin">Create a Card Direct PayIn</a>
     *
     * @param resourceId
     * @param timestamp
     * @param eventType
     * @return
     */
    @Operation(
        operationId = "webhook-02",
        summary     = "3D Secure",
        description = "3D secure validation redirect URL"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation"
    )
    @GetMapping(value = "/webhooks/payins/{payInKey}")
    String secureModeRedirectHandler(
        @Parameter(
            in = ParameterIn.PATH,
            required = true,
            description = "The PayIn platform unique identifier"
        )
        @PathVariable(name = "payInKey") UUID payInKey,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "The payment provider transaction identifier (PayIn identifier)"
        )
        @RequestParam(name = "transactionId", required = true) String transactionId
    );

}
