package eu.opertusmundi.web.controller.action;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.NotebookEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.Notebooks,
    description = "The notebooks API"
)
@RequestMapping(path = "/action/notebooks", produces = "application/json")
@Secured({"ROLE_CONSUMER", "ROLE_VENDOR_CONSUMER"})
public interface ConsumerNotebookController {

    /**
     * Get configuration for notebooks
     */
    @Operation(
        operationId = "consumer-notebooks-01",
        summary     = "Configuration",
        description = "Get notebook configuration information. The response, contains all available Jupyter images, "
                    + "user groups, user registered groups and available profiles. If the operation is successful, an "
                    + "instance of `ConfigurationResponse` is returned with Order details; Otherwise an instance of `BaseResponse` "
                    + "is returned with one or more error messages. Required role: `ROLE_CONSUMER`, `ROLE_VENDOR_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {
                BaseResponse.class, NotebookEndpointTypes.ConfigurationResponse.class
            })
        )
    )
    @GetMapping(value = "")
    RestResponse<?> getConfiguration();

    /**
     * Start server instance
     *
     * To start a new notebook server, no other server must be running
     *
     * @param profile
     * @return
     */
    @Operation(
        operationId = "consumer-notebooks-02",
        summary     = "Start server",
        description = "Start a notebook server for the selected profile. Any other running server must "
                    + "be stopped first. Required role: `ROLE_CONSUMER`, `ROLE_VENDOR_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {
                BaseResponse.class, NotebookEndpointTypes.UserStatusResponse.class
            })
        )
    )
    @PostMapping(value = "/server")
    RestResponse<?> startServer(
        @Parameter(
            in          = ParameterIn.QUERY,
            required    = true,
            description = "Profile unique name"
        )
        @RequestParam(name = "profile", required = true) String profileName
    );

    /**
     * Get server status
     *
     * @return
     */
    @Operation(
        operationId = "consumer-notebooks-03",
        summary     = "Start server",
        description = "Get server status. Required role: `ROLE_CONSUMER`, `ROLE_VENDOR_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {
                BaseResponse.class, NotebookEndpointTypes.UserStatusResponse.class
            })
        )
    )
    @GetMapping(value = "/server")
    RestResponse<?> getServerStatus();

    /**
     * Stop server instance
     *
     * @return
     */
    @Operation(
        operationId = "consumer-notebooks-04",
        summary     = "Stop server",
        description = "Stop running notebook server. Required role: `ROLE_CONSUMER`, `ROLE_VENDOR_CONSUMER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(oneOf = {
                BaseResponse.class, BaseResponse.class
            })
        )
    )
    @DeleteMapping(value = "/server")
    RestResponse<Void> stopServer();

}
