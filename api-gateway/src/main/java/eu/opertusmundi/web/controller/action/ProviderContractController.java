package eu.opertusmundi.web.controller.action;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.openapi.schema.ContractEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.ContractEndpointTypes.ProviderContractTemplateCollection;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.ContractProvider,
    description = "The contract API"
)
@RequestMapping(path = "/action/contract/provider", produces = "application/json")
@Secured({"ROLE_PROVIDER"})
public interface ProviderContractController {

    /**
     * Get all templates
     *
     * @return An instance of {@link ProviderContractTemplateCollection} class
     */
    @Operation(
        operationId = "provider-contract-01",
        summary     = "Find All",
        description = "Get all provider's contract templates. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderContractTemplateCollection.class)
        )
    )
    @GetMapping(value = "/templates")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findAll();

}
