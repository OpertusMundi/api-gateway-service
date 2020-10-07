package eu.opertusmundi.web.config;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfiguration {

    @Value("${springdoc.api-docs.server:http://localhost:8080}")
    private String serverUrl;

    @Value("${server.servlet.session.cookie.name}")
    private String sessionCookieName;

    /**
     * Provide configuration for OpenAPI auto-generated
     * configuration.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Opertus Mundi API Gateway")
                .version("1.0.0")
                .description(
                    "Opertus Mundi development server"
                )
                .termsOfService("https://opertusmundi.eu/terms/")
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://opertusmundi.eu/")
                )
            )
            .addServersItem(new Server()
                .url(this.serverUrl)
                .description("OpertusMundi - Development Server")
            )
            .components(new Components()
                .addSecuritySchemes(
                    "cookie",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(In.COOKIE)
                        .name(this.sessionCookieName)
                        .description("Cookie Authentication")
                )
            );
    }

    @Bean
    public OpenApiCustomiser orderedTagsOpenApiCustomiser() {
        return openApi -> {
            // Move Account and Profile tags first
            final Optional<Tag> accountTag = openApi.getTags()
                    .stream()
                    .filter(t-> t.getName().equalsIgnoreCase("Account"))
                    .findFirst();

            final Optional<Tag> profileTag = openApi.getTags()
                .stream()
                .filter(t-> t.getName().equalsIgnoreCase("Profile"))
                .findFirst();

            final List<Tag> tags = openApi.getTags()
                .stream()
                .filter(t-> !t.getName().equalsIgnoreCase("Account") && !t.getName().equalsIgnoreCase("Profile"))
                .sorted(Comparator.comparing(tag -> StringUtils.stripAccents(tag.getName())))
                .collect(Collectors.toList());

            if (profileTag.isPresent()) {
                tags.add(0, profileTag.get());
            }
            if (accountTag.isPresent()) {
                tags.add(0, accountTag.get());
            }

            openApi.setTags(tags);
        };
    }


}
