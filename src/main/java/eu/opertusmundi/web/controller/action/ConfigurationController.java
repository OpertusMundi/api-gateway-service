package eu.opertusmundi.web.controller.action;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.configuration.ClientConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for accessing global application configuration settings
 */
@Tag(
    name        = "Configuration",
    description = "The configuration API"
)
public interface ConfigurationController {

    /**
     * Get application configuration
     *
     * @param locale The locale used for resources
     * @return An instance of {@link ClientConfiguration} class
     */
    @Operation(
        summary     = "Get application configuration",
        description = "Get application configuration with optional localized resources",
        tags        = { "Configuration" }
    )
    @GetMapping(value = "/configuration/{locale}")
    RestResponse<ClientConfiguration> getConfiguration(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = false,
            description = "Selected user locale"
        )
        @PathVariable String locale
    );

}
