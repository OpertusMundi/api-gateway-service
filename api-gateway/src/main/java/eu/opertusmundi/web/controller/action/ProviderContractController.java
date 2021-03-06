package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.contract.helpdesk.EnumMasterContractSortField;
import eu.opertusmundi.common.model.contract.provider.EnumProviderContractSortField;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractCommandDto;
import eu.opertusmundi.web.model.openapi.schema.ContractEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.ContractEndpointTypes.ProviderContractTemplate;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.ContractProvider,
    description = "The provider contract API"
)
@RequestMapping(path = "/action/contract/provider", produces = "application/json")
@Secured({"ROLE_PROVIDER"})
public interface ProviderContractController {

    /**
     * Get all master contracts
     *
     * @return An instance of {@link ContractEndpointTypes#MasterContractCollection} class
     */
    @Operation(
        operationId = "provider-contract-01",
        summary     = "Query master templates",
        description = "Query master contract templates. This method is invoked by the provider client for initializing "
                    + "provider template creation. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.MasterContractCollection.class)
        )
    )
    @GetMapping(value = "/master")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findAllMasterContracts(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0") int page,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "25") @Max(50) @Min(1) int size,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Template title"
        )
        @RequestParam(name = "title", required = false) String title,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Sort field"
        )
        @RequestParam(name = "orderBy", defaultValue = "MODIFIED_ON") EnumMasterContractSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Sort order"
        )
        @RequestParam(name = "order", defaultValue = "DESC") EnumSortingOrder order
    );

    /**
     * Get master contract
     *
     * @return An instance of {@link ContractEndpointTypes#MasterContract} class
     */
    @Operation(
        operationId = "provider-contract-02",
        summary     = "Get master template",
        description = "Get a master contract template details by its unique key. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.MasterContract.class)
        )
    )
    @GetMapping(value = "/master/{key}")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findOneMasterContract(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Find template drafts
     *
     * @return An instance of {@link ContractEndpointTypes#ProviderDraftContractTemplateCollection} class
     */
    @Operation(
        operationId = "provider-contract-03",
        summary     = "Query drafts",
        description = "Query provider's contract template drafts. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderDraftContractTemplateCollection.class)
        )
    )
    @GetMapping(value = "/drafts")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findAllDrafts(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0") int page,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "10") int size,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order by property"
        )
        @RequestParam(name = "orderBy", defaultValue = "MODIFIED_ON") EnumProviderContractSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "DESC") EnumSortingOrder order
    );

    /**
     * Get template draft
     *
     * @return An instance of {@link ContractEndpointTypes#ProviderDraftContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-04",
        summary     = "Get draft",
        description = "Get provider's contract template draft by key. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderDraftContractTemplate.class)
        )
    )
    @GetMapping(value = "/drafts/{key}")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findOneDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Create contract template draft
     *
     * @return An instance of {@link ContractEndpointTypes#ProviderDraftContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-05",
        summary     = "Create draft",
        description = "Creates a new contract template draft. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderDraftContractTemplate.class)
        )
    )
    @PostMapping(value = "/drafts")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> createDraft(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contract update command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProviderTemplateContractCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        ProviderTemplateContractCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Update existing contract template
     *
     * @return An instance of {@link ContractEndpointTypes#ProviderDraftContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-06",
        summary     = "Update draft",
        description = "Updates an existing contract template. A draft template must already exists. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderDraftContractTemplate.class)
        )
    )
    @PostMapping(value = "/drafts/{key}")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> updateDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract unique key"
        )
        @PathVariable UUID key,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contract update command",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProviderTemplateContractCommandDto.class)
            ),
            required = true
        )
        @Valid
        @RequestBody
        ProviderTemplateContractCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Delete template draft
     *
     * @return An instance of {@link ProviderContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-07",
        summary     = "Delete draft",
        description = "Deletes a draft contract template. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderContractTemplate.class)
        )
    )
    @DeleteMapping(value = "/drafts/{key}")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> deleteDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Publish existing contract template
     *
     * @return An instance of {@link ContractEndpointTypes#ProviderContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-08",
        summary     = "Publish draft",
        description = "Publish a contract template draft. A draft template must already exists. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderContractTemplate.class)
        )
    )
    @PutMapping(value = "/drafts/{key}")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> publishDraft(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Find templates
     *
     * @return An instance of {@link ContractEndpointTypes#ProviderContractTemplateCollection} class
     */
    @Operation(
        operationId = "provider-contract-09",
        summary     = "Query contract templates",
        description = "Query provider's contract templates. Required roles: <b>ROLE_PROVIDER</b>"
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
    RestResponse<?> findAllTemplates(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0") int page,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "10") int size,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Order by property"
        )
        @RequestParam(name = "orderBy", defaultValue = "MODIFIED_ON") EnumProviderContractSortField orderBy,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Sorting order"
        )
        @RequestParam(name = "order", defaultValue = "DESC") EnumSortingOrder order
    );

    /**
     * Get template
     *
     * @return An instance of {@link ContractEndpointTypes#ProviderContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-10",
        summary     = "Get contract template",
        description = "Get provider's contract template by key. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderContractTemplate.class)
        )
    )
    @GetMapping(value = "/templates/{key}")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> findOneTemplate(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract template unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Delete (deactivate) a contract template
     *
     * @return An instance of {@link ProviderContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-12",
        summary     = "Deactivate template",
        description = "Deactivates a contract template. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderContractTemplate.class)
        )
    )
    @DeleteMapping(value = "/templates/{key}")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> deactivate(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract unique key"
        )
        @PathVariable UUID id
    );

    /**
     * Creates a new draft from an existing template.
     *
     * If a draft already exists, the existing record is returned
     *
     * @param id
     * @return
     */
    @Operation(
            operationId = "provider-contract-07",
            summary     = "Create draft from template",
            description = "Create a new draft from an existing contract template. The selected"
                        + "template must exist and have a status in [`ACTIVE`, `INACTIVE`]. Required roles: <b>ROLE_PROVIDER</b>"
        )
        @ApiResponse(
            responseCode = "200",
            description = "successful operation",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContractEndpointTypes.ProviderContractTemplate.class)
            )
        )
    @PostMapping(value = {"/history/{key}"})
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> createDraftForTemplate(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract unique key"
        )
        @PathVariable UUID key
    );

}
