package eu.opertusmundi.web.controller.action;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.util.StringUtils;

import eu.opertusmundi.common.domain.AssetDomainRestrictionEntity;
import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.CountryEntity;
import eu.opertusmundi.common.domain.CountryEuropeEntity;
import eu.opertusmundi.common.domain.LanguageEuropeEntity;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.EnumService;
import eu.opertusmundi.common.model.EnumSetting;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.SettingDto;
import eu.opertusmundi.common.model.pricing.PerCallPricingModelCommandDto;
import eu.opertusmundi.common.repository.AssetDomainRestrictionRepository;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.CountryRepository;
import eu.opertusmundi.common.repository.SettingRepository;
import eu.opertusmundi.web.model.configuration.ClientConfiguration;

@RestController
public class ConfigurationControllerImpl extends BaseController implements ConfigurationController {

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
    private ObjectMapper objectMapper;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AssetFileTypeRepository fileTypeRepository;

    @Autowired
    private AssetDomainRestrictionRepository domainRestrictionRepository;

    @Autowired
    private SettingRepository settingRepository;

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
            .forEach(config.getAuthProviders()::add);

        this.fileTypeRepository.findAllEnabled().stream()
            .map(AssetFileTypeEntity::toDto)
            .forEach(config.getAsset().getFileTypes()::add);

        this.domainRestrictionRepository.findAllActive().stream()
            .map(AssetDomainRestrictionEntity::getName)
            .forEach(config.getAsset().getDomains()::add);

        config.getWordPress().setEndpoint(wordPressEndpoint);

        config.getBuildInfo().setBuildTimestamp(buildTimestamp);
        config.getBuildInfo().setCommitComment(commitComment);
        config.getBuildInfo().setCommitId(commitId);
        config.getBuildInfo().setCommitIdDescription(commitIdDescription);

        setAnnouncement(config);
        setPrivateServicePricingModel(config);

        return config;
    }

    private void setAnnouncement(ClientConfiguration config) {
        final List<SettingDto> settings = settingRepository.findAllByServiceAsObjects(EnumService.API_GATEWAY);
        final SettingDto       content  = settings.stream()
            .filter(s -> s.getKey().equals(EnumSetting.MARKETPLACE_BANNER_TEXT.getKey()))
            .findFirst()
            .orElse(null);
        final SettingDto       enabled  = settings.stream()
            .filter(s -> s.getKey().equals(EnumSetting.MARKETPLACE_BANNER_ENABLED.getKey()))
            .findFirst()
            .orElse(null);

        if (content == null || enabled == null || StringUtils.isBlank(content.getValue())) {
            return;
        }

        if (enabled.asBoolean() || this.hasRole(EnumRole.ROLE_HELPDESK)) {
            config.setAnnouncement(ClientConfiguration.Announcement.of(content.getValue(), content.getUpdatedOn()));
        }
    }

    private void setPrivateServicePricingModel(ClientConfiguration config) {
        try {
            final var setting      = settingRepository.findOne(EnumSetting.USER_SERVICE_PRICE_PER_CALL);
            final var pricingModel = objectMapper.readValue(setting.getValue(), new TypeReference<PerCallPricingModelCommandDto>() { });

            config.getAsset().setPrivateServicePricingModel(pricingModel);
        } catch (final JsonProcessingException ex) {
            throw new ServiceException(BasicMessageCode.SerializationError, "Failed to parse the pricing model for private services ", ex);
        }
    }

}
