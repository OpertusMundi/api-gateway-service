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
import eu.opertusmundi.common.model.payment.EnumPayInItemSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayInDto;
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
    name        = EndpointTags.PayInProvider,
    description = "The provider PayIn API"
)
@RequestMapping(path = "/action/provider", produces = "application/json")
@Secured({"ROLE_PROVIDER", "ROLE_VENDOR_ANALYTICS"})
public interface ProviderPayInController {

    /**
     * Get PayIn item details
     *
     * @param payInKey
     * @param index
     * @return A {@link RestResponse} object with a result of type
     *         {@link PayInDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "provider-payin-01",
        summary     = "Get PayInItem",
        description = "Get PayInItem details. If the operation is successful, an instance of `ProviderPayInItemResponse` "
                    + "is returned with PayIn item details; Otherwise an instance of `BaseResponse` is returned with one "
                    + "or more error messages. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, PaymentEndPoints.ProviderPayInItemResponse.class})
        )
    )
    @GetMapping(value = "/payins/{payInKey}/{index}")
    RestResponse<?> findOnePayInItem(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "PayIn unique key"
            )
        @PathVariable UUID payInKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "PayIn item index. The index is 1-based."
            )
        @PathVariable Integer index
    );

    /**
     * Search provider PayIn item records
     *
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    @Operation(
        operationId = "provider-payin-02",
        summary     = "Provider PayInItems",
        description = "Search provider PayIn item records. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_ANALYTICS`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PaymentEndPoints.ProviderPayInItemCollectionResponse.class)
        )
    )
    @GetMapping(value = "/payins")
    RestResponse<?> findAllProviderPayInItems(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "PayIn status"
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
        @RequestParam(name = "orderBy", defaultValue = "EXECUTED_ON") EnumPayInItemSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "ASC") EnumSortingOrder order
    );

}
