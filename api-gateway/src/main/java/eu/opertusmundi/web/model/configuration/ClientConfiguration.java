package eu.opertusmundi.web.model.configuration;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.asset.AssetFileTypeDto;
import eu.opertusmundi.common.model.pricing.PricingModelSettings;
import eu.opertusmundi.common.model.spatial.CountryDto;
import eu.opertusmundi.common.model.spatial.CountryEuropeDto;
import eu.opertusmundi.common.model.spatial.LanguageDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Application configuration settings
 */
public class ClientConfiguration {

    @Schema(description = "Marketplace announcement data")
    @Getter
    @Setter
    @JsonInclude(Include.NON_NULL)
    private Announcement announcement;

    @Schema(description = "Asset related configuration settings")
    @Getter
    private final AssetConfiguration asset = new AssetConfiguration();

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported authentication methods"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
	private final List<EnumAuthProvider> authProviders = new ArrayList<EnumAuthProvider>();

    @Schema(description = "Build information")
    @Getter
    private final BuildVersionConfiguration buildInfo = new BuildVersionConfiguration();

    @ArraySchema(
        arraySchema = @Schema(
            description = "World countries"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    private final List<CountryDto> countries = new ArrayList<CountryDto>();

    @ArraySchema(
        arraySchema = @Schema(
            description = "European countries"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    private final List<CountryEuropeDto> europeCountries = new ArrayList<CountryEuropeDto>();

    @ArraySchema(
        arraySchema = @Schema(
            description = "European languages"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    private final List<LanguageDto> europeLanguages = new ArrayList<LanguageDto>();

    @Schema(description = "Sentinel Hub configuration settings")
    @Getter
    @Setter
    @JsonInclude(Include.NON_NULL)
    private SentinelHubConfiguration sentinelHub;

    @Schema(description = "WordPress configuration settings")
    @Getter
    private final WordPressConfiguration wordPress = new WordPressConfiguration();

    public static class AssetConfiguration {

        @ArraySchema(
            arraySchema = @Schema(
                description = "Supported asset file types"
            ),
            minItems = 0,
            uniqueItems = true
        )
        @Getter
        private final List<AssetFileTypeDto> fileTypes = new ArrayList<>();

        @ArraySchema(
            arraySchema = @Schema(
                description = "Available pricing models"
            ),
            minItems = 1,
            uniqueItems = true
        )
        @Getter
        private final List<PricingModelSettings> pricingModels = new ArrayList<>();

        @ArraySchema(
            arraySchema = @Schema(
                description = "Asset domains"
            ),
            minItems = 1,
            uniqueItems = true
        )
        @Getter
        private final List<String> domains = new ArrayList<>();

    }

    @AllArgsConstructor
    @Builder
    @Getter
    public static class SentinelHubConfiguration {

        @Schema(description = "Sentinel Hub instance id")
        private final String instanceId;
    }

    public static class WordPressConfiguration {

        @Schema(description = "WordPress instance endpoint")
        @Getter
        @Setter
        private  String endpoint;

    }

    @Getter
    @Setter
    public static class BuildVersionConfiguration {

        @Schema(description = "Commit identifier")
        private String commitId;

        @Schema(description = "Commit comment")
        private String commitComment;

        @Schema(description = "Commit identifier description")
        private String commitIdDescription;

        @Schema(description = "Build timestamp")
        private String buildTimestamp;

    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    public static class Announcement {

        @Schema(description = "Announcement HTML content")
        private final String content;

        @Schema(description = "Date of last update")
        private final ZonedDateTime modifiedOn;

    }

}
