package eu.opertusmundi.web.config;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(
    basePackageClasses = {
        eu.opertusmundi.web.repository._Marker.class,
    }
)
@EnableTransactionManagement(mode = AdviceMode.PROXY)
public class DatabaseConfiguration {

}
