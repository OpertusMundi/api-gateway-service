package eu.opertusmundi.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(
    scanBasePackageClasses = {
        eu.opertusmundi.common.config._Marker.class,
        eu.opertusmundi.common.repository._Marker.class,
        eu.opertusmundi.common.service._Marker.class,
        eu.opertusmundi.web.config._Marker.class,
        eu.opertusmundi.web.controller._Marker.class,
        eu.opertusmundi.web.repository._Marker.class,
        eu.opertusmundi.web.security._Marker.class,
        eu.opertusmundi.web.service._Marker.class,
        eu.opertusmundi.web.util._Marker.class,
        eu.opertusmundi.web.validation._Marker.class,
    }
)
@EntityScan(
    basePackageClasses = {
        eu.opertusmundi.common.domain._Marker.class,
        eu.opertusmundi.web.domain._Marker.class,
    }
)
public class Application extends SpringBootServletInitializer {

    /**
     * Used when packaging as a WAR application
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    {
        return builder.sources(Application.class);
    }

    /**
     * Used when packaging as a standalone JAR (the server is embedded)
     */
    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }
}
