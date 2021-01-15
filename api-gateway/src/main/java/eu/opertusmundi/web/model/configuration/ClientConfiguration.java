package eu.opertusmundi.web.model.configuration;

import java.util.ArrayList;
import java.util.List;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.asset.AssetFileTypeDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

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
    @Getter
	private final List<EnumAuthProvider> authProviders = new ArrayList<EnumAuthProvider>();

    @Schema(description = "Asset related configuration settings")
    @Getter
    private final AssetConfiguration asset = new AssetConfiguration();

    public static class AssetConfiguration {

        @ArraySchema(
            arraySchema = @Schema(
                description = "Supported asset file types"
            ),
            minItems = 0,
            uniqueItems = true
        )
        @Getter
        private final List<AssetFileTypeDto> fileTypes = new ArrayList<AssetFileTypeDto>();

    }

}
