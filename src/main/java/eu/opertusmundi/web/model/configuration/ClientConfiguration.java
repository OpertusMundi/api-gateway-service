package eu.opertusmundi.web.model.configuration;

import java.util.ArrayList;
import java.util.List;

import eu.opertusmundi.common.model.EnumAuthProvider;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Application configuration settings
 */
public class ClientConfiguration {

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported authentication methods"
        ),
        minItems = 0,
        uniqueItems = true
    )
	private final List<EnumAuthProvider> authProviders = new ArrayList<EnumAuthProvider>();

	public List<EnumAuthProvider> getAuthProviders() {
		return this.authProviders;
	}

}
