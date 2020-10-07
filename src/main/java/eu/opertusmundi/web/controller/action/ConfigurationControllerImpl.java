package eu.opertusmundi.web.controller.action;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.model.configuration.ClientConfiguration;

@RestController
@RequestMapping(path = "/action", produces = "application/json")
public class ConfigurationControllerImpl implements ConfigurationController {

    @Value("${opertus-mundi.authentication-providers:forms}")
    private String authProviders;

    @Override
    public RestResponse<ClientConfiguration> getConfiguration(String locale) {
        return RestResponse.result(this.createConfiguration());
    }

    private ClientConfiguration createConfiguration() {
        final ClientConfiguration config = new ClientConfiguration();

        Arrays.stream(this.authProviders.split(","))
            .map(String::trim)
            .map(EnumAuthProvider::fromString)
            .filter(s -> s != null)
            .forEach(s -> config.getAuthProviders().add(s));

        return config;
    }

}
