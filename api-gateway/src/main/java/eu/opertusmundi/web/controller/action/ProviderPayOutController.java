package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.payment.EnumPayOutSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.PaymentEndPoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.PayOutProvider,
    description = "The provider PayOut API"
)
@RequestMapping(path = "/action/provider", produces = "application/json")
@Secured({"ROLE_PROVIDER"})
public interface ProviderPayOutController {

    /**
     * Get PayOut details
     *
     * @param payOutKey
     * @return A {@link RestResponse} object with a result of type
     *         {@link PayOutDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "provider-payout-01",
        summary     = "Get PayOut",
        description = "Get PayOut details. If the operation is successful, an instance of `PayOutResponse` "
                    + "is returned with PayOut details; Otherwise an instance of `BaseResponse` "
                    + "is returned with one or more error messages. Roles required: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {
                BaseResponse.class, PaymentEndPoints.PayOutResponse.class
            })
        )
    )
    @GetMapping(value = "/payouts/{payOutKey}")
    RestResponse<?> findOnePayOut(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "PayOut unique key"
            )
        @PathVariable UUID payOutKey
    );

    /**
     * Search provider PayOut records
     *
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "provider-payout-02",
        summary     = "Provider PayOuts",
        description = "Search provider PayOut records. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = PaymentEndPoints.PayOutCollectionResponse.class)
        )
    )
    @GetMapping(value = "/payouts")
    RestResponse<?> findAllProviderPayOuts(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "PayOut status"
        )
        @RequestParam(name = "status", required = false) EnumTransactionStatus status,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0") int pageIndex,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "10") int pageSize,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order by property"
        )
        @RequestParam(name = "orderBy", defaultValue = "EXECUTED_ON") EnumPayOutSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

}
