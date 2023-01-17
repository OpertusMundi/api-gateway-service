package eu.opertusmundi.web.controller.action;

import java.util.Set;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountTicketCommandDto;
import eu.opertusmundi.common.model.account.EnumTicketStatus;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.MessageEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.TicketEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RequestMapping(path = "/action/consumer/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
public interface ConsumerTicketController {

    /**
     * Find tickets
     *
     * @param pageIndex
     * @param pageSize
     * @param status
     * @return
     */
    @Operation(
        operationId = "ticket-01",
        summary     = "Find tickets",
        description = "Find the tickets of the authenticated user. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.ConsumerTickets }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = TicketEndpointTypes.TicketListResponseDto.class)
        )
    )
    @GetMapping(value = "")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    RestResponse<?> find(
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
            description = "Filter tickets by status"
        )
        @RequestParam(name = "status", required = false) Set<EnumTicketStatus> status
    );

    /**
     * Open a new ticket
     *
     * @param command Ticket command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @Operation(
        operationId = "ticket-02",
        summary     = "Open a ticket to Helpdesk",
        description = "Opens a ticket to Helpdesk from the authenticated user. Required role: `ROLE_USER`, `ROLE_VENDOR_USER`",
        tags        = { EndpointTags.ConsumerTickets }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MessageEndpointTypes.MessageResponseDto.class))
    )
    @PostMapping(value = "")
    @Secured({"ROLE_USER", "ROLE_VENDOR_USER"})
    @Validated
    RestResponse<?> openTicket(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Ticket",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccountTicketCommandDto.class)),
            required = true
        )
        @RequestBody(required = true) @Valid AccountTicketCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

}
