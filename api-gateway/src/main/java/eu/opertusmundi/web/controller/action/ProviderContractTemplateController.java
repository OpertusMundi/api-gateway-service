package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.ContractProviderTemplates,
    description = "The provider contract template API"
)
@RequestMapping(path = "/action/contract/provider", produces = "application/json")
public interface ProviderContractTemplateController {

    /**
     * Get all master contracts
     *
     * @return An instance of {@link ContractEndpointTypes#MasterContractCollection} class
     */
    @Operation(
        operationId = "provider-contract-template-01",
        summary     = "Query master templates",
        description = "Query master contract templates. This method is invoked by the provider client for initializing "
                    + "provider template creation. Required role: `ROLE_PROVIDER`"
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
        operationId = "provider-contract-template-02",
        summary     = "Get master template",
        description = "Get a master contract template details by its unique key. Required role: `ROLE_PROVIDER`"
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
        operationId = "provider-contract-template-03",
        summary     = "Query drafts",
        description = "Query provider's contract template drafts. Required role: `ROLE_PROVIDER`"
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
        operationId = "provider-contract-template-04",
        summary     = "Get draft",
        description = "Get provider's contract template draft by key. Required role: `ROLE_PROVIDER`"
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
        operationId = "provider-contract-template-05",
        summary     = "Create draft",
        description = "Creates a new contract template draft. Required role: `ROLE_PROVIDER`"
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
        operationId = "provider-contract-template-06",
        summary     = "Update draft",
        description = "Updates an existing contract template. A draft template must already exists. Required role: `ROLE_PROVIDER`"
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
        operationId = "provider-contract-template-07",
        summary     = "Delete draft",
        description = "Deletes a draft contract template. Required role: `ROLE_PROVIDER`"
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
        operationId = "provider-contract-template-08",
        summary     = "Publish draft",
        description = "Publish a contract template draft. A draft template must already exists. Required role: `ROLE_PROVIDER`"
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
        operationId = "provider-contract-template-09",
        summary     = "Query contract templates",
        description = "Query provider's contract templates. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
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
    @Secured({"ROLE_PROVIDER", "ROLE_VENDOR_PROVIDER"})
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
        operationId = "provider-contract-template-10",
        summary     = "Get contract template",
        description = "Get provider's contract template by key. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderContractTemplate.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "not found"
    )
    @GetMapping(value = "/templates/{key}")
    @Secured({"ROLE_PROVIDER", "ROLE_VENDOR_PROVIDER"})
    RestResponse<?> findOneTemplate(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract template unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Print contract template
     *
     * @param templateKey
     * @param response
     * @return
     * @throws IOException
     */
    @Operation(
        operationId = "provider-contract-template-11",
        summary     = "Print template",
        description = "Prints a contract template using sample data. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`",
        security    = {
            @SecurityRequirement(name = "cookie")
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful Request",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The requested contract file"))
    )
    @GetMapping(value = "/templates/pdf/{key}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Validated
    @Secured({"ROLE_PROVIDER", "ROLE_VENDOR_PROVIDER"})
    ResponseEntity<StreamingResponseBody> printTemplate(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Template unique key"
        )
        @PathVariable(name = "key", required = true) UUID templateKey,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException;

    /**
     * Delete (deactivate) a contract template
     *
     * @return An instance of {@link ProviderContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-template-12",
        summary     = "Deactivate template",
        description = "Deactivates a contract template. Required role: `ROLE_PROVIDER`"
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
        @PathVariable UUID key
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
            operationId = "provider-contract-template-13",
            summary     = "Create draft from template",
            description = "Create a new draft from an existing contract template. The selected"
                        + "template must exist and have a status in [`ACTIVE`, `INACTIVE`]. Required role: `ROLE_PROVIDER`"
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
    RestResponse<?> createDraftFromTemplate(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Contract unique key"
        )
        @PathVariable UUID key
    );

    /**
     * Accept provider default contract template
     *
     * @return An instance of {@link ContractEndpointTypes#ProviderContractTemplate} class
     */
    @Operation(
        operationId = "provider-contract-template-14",
        summary     = "Accept default contract",
        description = "Mark provider default contract template as accepted. Required role: `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ContractEndpointTypes.ProviderContractTemplate.class)
        )
    )
    @PutMapping(value = "/default-contract")
    @Secured({"ROLE_PROVIDER"})
    RestResponse<?> acceptDefaultContract();
}
