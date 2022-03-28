package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationCommandDto;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.QuotationEndpoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.Quotation,
    description = "The quotation API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface QuotationController {

    /**
     * Computes a quotation
     *
     * @return A {@link RestResponse} object with a result of type
     *         {@link EffectivePricingModelDto} if operation was successful; Otherwise an
     *         instance of {@link BaseResponse} is returned with one or more error
     *         messages
     */
    @Operation(
        operationId = "quotation-01",
        summary     = "Create quotation",
        description = "Creates a quotation for the specified pricing model and quotation parameters. "
                    + "If the operation is successful, an instance of `QuotationResponse` is returned with "
                    + "the new quotation; Otherwise an instance of `BaseResponse` is returned with "
                    + "one or more error messages"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {BaseResponse.class, QuotationEndpoints.QuotationResponse.class})
        )
    )
    @PostMapping(value = "/quotation", consumes = "application/json")
    @Validated
    RestResponse<?> createQuotation(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Quotation command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuotationCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        QuotationCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

}
