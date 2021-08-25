package eu.opertusmundi.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile({"!testing"})
@Configuration
@PropertySource("classpath:git.properties")
public class GitPropertySourceConfig {

}
