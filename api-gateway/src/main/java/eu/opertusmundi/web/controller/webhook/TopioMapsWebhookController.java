package eu.opertusmundi.web.controller.webhook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = EndpointTags.API_TopioMaps)
@SecurityRequirement(name = "jwt")
public interface TopioMapsWebhookController {

    /**
     * Handles Topio Maps webhook events
     *
     * @param timestamp
     * @param eventType
     * @param params
     * @return
     */
    @Operation(
        operationId = "topio-maps-01",
        summary     = "Topio Maps",
        description = "Handles Topio Maps webhook events"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Operation"
    )
    @PostMapping(value = "/api/webhooks/topio-maps")
    ResponseEntity<Void> webhookHandler(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "When the event happened"
        )
        @RequestParam(name = "Date", required = true) Long timestamp,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "The event type",
            schema = @Schema(allowableValues = {"MAP_CREATED", "MAP_DELETED"})
        )
        @RequestParam(name = "EventType", required = true) String eventType,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Event attributes"
        )
        @RequestBody ObjectNode attributes

    );

}
