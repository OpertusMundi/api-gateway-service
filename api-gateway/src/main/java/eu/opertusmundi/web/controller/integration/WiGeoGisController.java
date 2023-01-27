package eu.opertusmundi.web.controller.integration;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.openapi.schema.WiGeoGisEndpointTypes;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = EndpointTags.WiGeoGIS)
@RequestMapping(path = "/action/integration/wigeogis", produces = MediaType.APPLICATION_JSON_VALUE)
@Secured({"ROLE_WIGEOGIS"})
public interface WiGeoGisController {

    /**
     * Login to the authenticated user's WiGeoGIS account and return a redirect link
     *
     * @return A link to the WiGeoGIS application
     */
    @Operation(
        operationId = "integration-wigeogis-01",
        summary     = "Login",
        description = """
           Login to the authenticated user's WiGeoGIS account and return a redirect link. If the
           operation fails, a null string is returned. Required role: `ROLE_WIGEOGIS`
        """
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = WiGeoGisEndpointTypes.LoginResponse.class)
        )
    )
    @PostMapping(value = "/login")
    RestResponse<?> login();

}
