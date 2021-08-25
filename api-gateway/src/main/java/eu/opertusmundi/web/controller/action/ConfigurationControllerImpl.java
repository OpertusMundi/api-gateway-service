package eu.opertusmundi.web.controller.action;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.CountryEntity;
import eu.opertusmundi.common.domain.CountryEuropeEntity;
import eu.opertusmundi.common.domain.LanguageEuropeEntity;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.CountryRepository;
import eu.opertusmundi.web.model.configuration.ClientConfiguration;

@RestController
@RequestMapping(path = "/action", produces = "application/json")
public class ConfigurationControllerImpl implements ConfigurationController {

    @Value("${opertus-mundi.authentication-providers:forms}")
    private String authProviders;

    @Value("${opertus-mundi.wordpress.endpoint:}")
    private String wordPressEndpoint;

    @Value("${git.commit.id.abbrev:}")
    String commitId;

    @Value("${git.commit.message.short:}")
    String commitComment;

    @Value("${git.commit.id.describe:}")
    String commitIdDescription;

    @Value("${git.build.time:}")
    String buildTimestamp;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;

    @Override
    public RestResponse<ClientConfiguration> getConfiguration(String locale) {
        return RestResponse.result(this.createConfiguration());
    }

    private ClientConfiguration createConfiguration() {
        final ClientConfiguration config = new ClientConfiguration();

        this.countryRepository.getCountries().stream()
            .map(CountryEntity::toDto)
            .forEach(c -> config.getCountries().add(c));

        this.countryRepository.getEuropeCountries().stream()
            .map(CountryEuropeEntity::toDto)
            .forEach(c -> config.getEuropeCountries().add(c));

        this.countryRepository.getEuropeLanguages().stream()
            .map(LanguageEuropeEntity::toDto)
            .forEach(l -> config.getEuropeLanguages().add(l));

        Arrays.stream(this.authProviders.split(","))
            .map(String::trim)
            .map(EnumAuthProvider::fromString)
            .filter(s -> s != null)
            .forEach(s -> config.getAuthProviders().add(s));

        this.assetFileTypeRepository.findAllEnabled().parallelStream()
            .map(AssetFileTypeEntity::toDto)
            .forEach(t -> config.getAsset().getFileTypes().add(t));

        config.getWordPress().setEndpoint(wordPressEndpoint);

        config.getBuildInfo().setBuildTimestamp(buildTimestamp);
        config.getBuildInfo().setCommitComment(commitComment);
        config.getBuildInfo().setCommitId(commitId);
        config.getBuildInfo().setCommitIdDescription(commitIdDescription);

        return config;
    }

}
