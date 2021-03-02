package eu.opertusmundi.web.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableFeignClients(
    basePackageClasses = {
        eu.opertusmundi.common.feign.client._Marker.class,
        eu.opertusmundi.web.feign.client._Marker.class,
    }
)
@PropertySource("classpath:config/feign-client.properties")
public class FeignClientConfiguration {

    // Add any custom bean definitions here

}
