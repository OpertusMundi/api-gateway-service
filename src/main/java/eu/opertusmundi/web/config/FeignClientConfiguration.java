package eu.opertusmundi.web.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(
    basePackageClasses = {
        eu.opertusmundi.web.feign.client._Marker.class,
    }
)
public class FeignClientConfiguration {

    // Add any custom bean definitions here

}
