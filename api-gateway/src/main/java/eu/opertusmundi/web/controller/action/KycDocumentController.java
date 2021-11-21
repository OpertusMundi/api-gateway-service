package eu.opertusmundi.web.controller.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.kyc.KycDocumentCommandDto;
import eu.opertusmundi.common.model.kyc.KycDocumentDto;
import eu.opertusmundi.common.model.kyc.KycDocumentPageCommandDto;
import eu.opertusmundi.common.model.openapi.schema.KycDocumentEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.KycDocument,
    description = "The KYC document API"
)
@RequestMapping(path = "/action", produces = "application/json")
@Secured({"ROLE_CONSUMER", "ROLE_PROVIDER"})
public interface KycDocumentController {

    /**
     * Enumerate all KYC documents
     *
     * @param pageIndex Page index
     * @param pageSize Page size
     *
     * @return An instance of {@link KycDocumentEndpointTypes#KycDocumentListResponse} class
     */
    @Operation(
        operationId = "kyc-01",
        summary     = "Search Documents",
        description = "Enumerate all KYC documents for the authenticated provider or consumer. "
                    + "Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = KycDocumentEndpointTypes.KycDocumentListResponse.class)
        )
    )
    @GetMapping(value = "/kyc-documents")
    RestResponse<?> findAllDocuments(
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Customer type",
            example = "PROVIDER"
        )
        @RequestParam(name = "type") EnumCustomerType customerType,
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
     * Get KYC document
     *
     * @param kycDocumentId The document unique identifier
     * @return A {@link RestResponse} with a result of type {@link KycDocumentDto}
     */
    @Operation(
        operationId = "kyc-02",
        summary     = "Get Document",
        description = "Get a single KYC document by its unique identifier. Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = KycDocumentEndpointTypes.KycDocumentResponse.class)
        )
    )
    @GetMapping(value = "/kyc-documents/{kycDocumentId}")
    RestResponse<KycDocumentDto> findOneDocument(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "KYC document unique identifier"
        )
        @PathVariable String kycDocumentId,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Customer type",
            example = "PROVIDER"
        )
        @RequestParam(name = "type") EnumCustomerType customerType
    );

    /**
     * Create new KYC document
     *
     * @param command The document to create
     * @return
     */
    @Operation(
        operationId = "kyc-03",
        summary     = "Create Document",
        description = "Creates a draft KYC document with status `CREATED`. Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentEndpointTypes.KycDocumentResponse.class))
    )
    @Validated
    @PostMapping(value = "/kyc-documents")
    RestResponse<KycDocumentDto> createKycDocument(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "KYC document command",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentCommandDto.class)),
            required = true
        )
        @Valid
        @RequestBody
        KycDocumentCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Add page
     *
     * @param kycDocumentId The KYC document unique identifier
     * @param file
     * @param validationResult
     * @return
     */
    @Operation(
        operationId = "kyc-04",
        summary     = "Add Page",
        description = "Adds a new page to a draft KYC document. Pages can be added only to documents with "
                    + "status `CREATED`. The maximum size per page is `7MB`. The supported formats for the documents "
                    + "are `pdf`, `jpeg`, `jpg` and `png`. The minimum size is `1Kb`. Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @PostMapping(value = "/kyc-documents/{kycDocumentId}/pages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Validated
    BaseResponse addPage(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "KYC document unique identifier"
        )
        @PathVariable String kycDocumentId,
        @Parameter(schema = @Schema(
            name = "file", type = "string", format = "binary", description = "Uploaded file"
        ))
        @NotNull @RequestPart(name = "file", required = true) MultipartFile file,
        @Valid @RequestPart(name = "data", required = true) KycDocumentPageCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

    /**
     * Submit Document
     *
     * @param kycDocumentId The KYC document unique identifier
     * @return
     */
    @Operation(
        operationId = "kyc-05",
        summary     = "Submit Document",
        description = "Submit a draft KYC document for validation. The document status must be `CREATED` . "
                    + "Required role: `ROLE_CONSUMER`, `ROLE_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentEndpointTypes.KycDocumentResponse.class))
    )
    @PutMapping(value = "/kyc-documents/{kycDocumentId}")
    @Validated
    RestResponse<KycDocumentDto> submitKycDocument(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "KYC document unique identifier"
        )
        @PathVariable String kycDocumentId,
        @Valid
        @RequestBody
        KycDocumentCommandDto command,
        @Parameter(
            hidden = true
        )
        BindingResult validationResult
    );

}
