package eu.opertusmundi.web.config;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import eu.opertusmundi.common.model.jupyter.JupyterHubProfile;

@Configuration
@ConfigurationProperties(prefix = "opertusmundi.jupyterhub", ignoreUnknownFields = true)
@PropertySource(value = "${opertusmundi.jupyterhub.config}", ignoreResourceNotFound = true)
@Validated
@lombok.Getter
@lombok.Setter
public class JupyterHubConfiguration implements InitializingBean
{
    private static final Logger logger = LoggerFactory.getLogger(JupyterHubConfiguration.class);

    /**
     * The public URL for JupyterHub application
     */
    @NotNull
    private URL url;

    /**
     * The list of available server profiles
     */
    private List<JupyterHubProfile> profiles = Collections.emptyList();

    @Override
    public void afterPropertiesSet() throws Exception
    {
        logger.info("Pointing at {}. The following server profiles are present: {}",
            url, profiles.stream().map(JupyterHubProfile::getName).toArray());
    }

    public JupyterHubProfile getProfileByName(String name) {
        return this.profiles.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }
}
