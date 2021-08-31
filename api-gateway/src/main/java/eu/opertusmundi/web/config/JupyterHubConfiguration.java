package eu.opertusmundi.web.config;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import eu.opertusmundi.common.model.jupyter.JupyterHubProfile;

@Configuration
@ConfigurationProperties(prefix = "opertusmundi.jupyterhub", ignoreUnknownFields = true)
@PropertySource(value = "file:config/jupyterhub.properties", ignoreResourceNotFound = true)
@lombok.Getter
@lombok.Setter
public class JupyterHubConfiguration
{
    /**
     * The base URL for JupyterHub application
     */
    private URL url;
    
    /**
     * The access token for the admin of JupyterHub
     */
    private String accessToken;
    
    /**
     * The list of available server profiles 
     */
    private List<JupyterHubProfile> profiles = Collections.emptyList();
}
