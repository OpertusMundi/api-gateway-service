package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;

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

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.kyc.UboCommandDto;
import eu.opertusmundi.common.model.kyc.UboDeclarationDto;
import eu.opertusmundi.common.model.kyc.UboDto;
import eu.opertusmundi.common.model.openapi.schema.UboDeclarationEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.UboDeclaration,
    description = "The UBO declaration API"
)
@RequestMapping(path = "/action", produces = "application/json")
@Secured({"ROLE_PROVIDER"})
public interface UboDeclarationController {

    /**
     * Enumerate all UBO declarations
     *
     * @param pageIndex Page index
     * @param pageSize Page size
     *
     * @return An instance of {@link UboDeclarationEndpointTypes#UboDeclarationListResponse} class
     */
    @Operation(
        operationId = "ubo-01",
        summary     = "Search Declarations",
        description = "Enumerate all UBO declarations for the authenticated provider. "
                    + "Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = UboDeclarationEndpointTypes.UboDeclarationListResponse.class)
        )
    )
    @GetMapping(value = "/ubo-declarations")
    RestResponse<?> findAllDeclarations(
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Page index"
        )
        @RequestParam(name = "page", defaultValue = "0", required = false) int pageIndex,
        @Parameter(
            in = ParameterIn.QUERY,
            required = false,
            description = "Page size"
        )
        @RequestParam(name = "size", defaultValue = "10", required = false) int pageSize
    );
    
    /**
     * Get UBO declaration
     *
     * @param uboDeclarationId The declaration unique identifier
     * @return A {@link RestResponse} with a result of type {@link UboDeclarationDto}
     */
    @Operation(
        operationId = "ubo-02",
        summary     = "Get Declaration",
        description = "Get a single UBO declaration by its unique identifier. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = UboDeclarationEndpointTypes.UboDeclarationResponse.class)
        )
    )
    @GetMapping(value = "/ubo-declarations/{uboDeclarationId}")
    RestResponse<UboDeclarationDto> findOneDeclaration(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "UBO declaration unique identifier"
        )
        @PathVariable String uboDeclarationId
    );
    
    /**
     * Create new UBO declaration
     *
     * @param command The declaration to create
     * @return
     */
    @Operation(
        operationId = "ubo-03",
        summary     = "Create Declaration",
        description = "Creates a draft UBO declaration with status `CREATED`. Only a single UBO declaration with status `CREATED`, "
                    + "`INCOMPLETE` or `VALIDATION_ASKED` can exist. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UboDeclarationEndpointTypes.UboDeclarationResponse.class))
    )
    @PostMapping(value = "/ubo-declarations")
    RestResponse<UboDeclarationDto> createUboDeclaration();

    /**
     * Add UBO
     *
     * @param uboDeclarationId The UBO declaration unique identifier
     * @param command The new UBO
     * @return
     */
    @Operation(
        operationId = "ubo-04",
        summary     = "Add UBO",
        description = "Adds a new UBO to the draft UBO declaration. Only records with status `CREATED` or `INCOMPLETE` can be updated. "
                    + "Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UboDeclarationEndpointTypes.UboResponse.class))
    )
    @PostMapping(value = "/ubo-declarations/{uboDeclarationId}/ubos", consumes = "application/json")
    @Validated
    RestResponse<UboDto> addUbo(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "UBO declaration unique identifier"
        )
        @PathVariable String uboDeclarationId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "UBO command",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UboCommandDto.class)),
            required = true
        )
        @Valid
        @RequestBody
        UboCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
   
    /**
     * Update UBO
     *
     * @param uboDeclarationId The UBO declaration unique identifier
     * @param uboId The UBO unique identifier
     * @param command The updated UBO
     * @return
     */
    @Operation(
        operationId = "ubo-05",
        summary     = "Update UBO",
        description = "Updates an existing UBO of the draft UBO declaration. Only records with status `CREATED` "
                    + "or `INCOMPLETE`can be updated. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UboDeclarationEndpointTypes.UboResponse.class))
    )
    @PutMapping(value = "/ubo-declarations/{uboDeclarationId}/ubos/{uboId}", consumes = "application/json")
    @Validated
    RestResponse<UboDto> updateUbo(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "UBO declaration unique identifier"
        )
        @PathVariable String uboDeclarationId,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "UBO unique identifier"
        )
        @PathVariable String uboId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "UBO command",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UboCommandDto.class)),
            required = true
        )
        @Valid
        @RequestBody
        UboCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );
    
    /**
     * Remove UBO
     *
     * @param uboDeclarationId The UBO declaration unique identifier
     * @param uboId The UBO unique identifier
     * @return
     */
    @Operation(
        operationId = "ubo-06",
        summary     = "Remove UBO",
        description = "Removes a UBO from the draft UBO declaration. Only records with status `CREATED` "
                    + "or `INCOMPLETE` can be updated. Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/ubo-declarations/{uboDeclarationId}/ubos/{uboId}")
    @Validated
    BaseResponse removeUbo(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "UBO declaration unique identifier"
        )
        @PathVariable String uboDeclarationId,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "UBO unique identifier"
        )
        @PathVariable String uboId
    );
    
    /**
     * Submit UBO
     *
     * @param uboDeclarationId The UBO declaration unique identifier
     * @return
     */
    @Operation(
        operationId = "ubo-07",
        summary     = "Submit Declaration",
        description = "Submit the draft UBO declaration for validation. The declaration status must be `CREATED` or `INCOMPLETE`. "
                    + "Required roles: <b>ROLE_PROVIDER</b>"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UboDeclarationEndpointTypes.UboDeclarationResponse.class))
    )
    @PostMapping(value = "/ubo-declarations/{uboDeclarationId}")
    RestResponse<UboDeclarationDto> submitUboDeclaration(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "UBO declaration unique identifier"
        )
        @PathVariable String uboDeclarationId
    );
 
}
