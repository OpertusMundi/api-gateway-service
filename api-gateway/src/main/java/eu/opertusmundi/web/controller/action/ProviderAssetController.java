package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = "Provider Assets",
    description = "The provider asset API"
)
@RequestMapping(path = "/action", produces = "application/json")
public interface ProviderAssetController {

    /**
     * Delete catalogue item
     *
     * @param id The item unique id
     * @return
     */
    @Operation(
        operationId = "provider-assets-01",
        summary     = "Delete asset",
        description = "Delete asset from catalogue. Required roles: <b>ROLE_PROVIDER</b>",
        tags        = { "Provider Assets" }
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))
    )
    @DeleteMapping(value = "/provider/assets/{id}")
    @Secured({"ROLE_PROVIDER"})
    BaseResponse deleteAsset(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Item unique id"
        )
        @PathVariable UUID id
    );

}
