package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.ContractConsumer,
    description = "The contract API"
)
@RequestMapping(path = "/action/contract/consumer", produces = "application/json")
@Secured({"ROLE_CONSUMER"})
public interface ConsumerContractController {

    /**
     * Print contract
     *
     * @param orderKey
     * @param response
     * @return
     * @throws IOException
     */
    @Operation(
        operationId = "consumer-contract-01",
        summary     = "Print Contract",
        description = "Prints a contract for the specified order item. Currently only one contract "
                    + "exists since orders can have only a single item. Required role: `ROLE_CONSUMER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested contract file"))
    )
    @PostMapping(value = "/order/{key}", params = {"index"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Validated
    ResponseEntity<StreamingResponseBody> print(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable(name = "key", required = true) UUID orderKey,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = true,
            description = "Order item index. Index is 1-based."
        )
        @RequestParam(name = "index") @Min(1) Integer itemIndex,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

    /**
     * Download contract
     *
     * @param orderKey
     * @param itemIndex
     * @param signed
     * @param response
     * @return
     */
    @Operation(
        operationId = "consumer-contract-02",
        summary     = "Download Contract",
        description = "Downloads the contract for the specified order item. Currently only one contract "
                    + "exists since orders may have only a single item. Required role: `ROLE_CONSUMER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested contract file"))
    )
    @GetMapping(value = "/order/{key}", params = {"index", "signed"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Validated
    ResponseEntity<StreamingResponseBody> download(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Order unique key"
        )
        @PathVariable(name = "key", required = true) UUID orderKey,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = false,
            description = "Order item index. Index is 1-based."
        )
        @RequestParam(name = "index", required = false, defaultValue = "1") @Min(1) Integer itemIndex,
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = true,
            description = "True if a signed contract is requested. For unsigned contracts, `print` action must be invoked first."
                        + "For signed ones, both `print` and `sign` methods must be invoked before downloading the file; Otherwise "
                        + "a `404` status code will be returned."
        )
        @RequestParam(name = "signed", required = true) boolean signed,
        @Parameter(hidden = true)
        HttpServletResponse response
    );

}
