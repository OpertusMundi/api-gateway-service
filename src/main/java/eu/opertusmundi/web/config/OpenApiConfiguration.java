package eu.opertusmundi.web.config;

import java.util.Arrays;
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
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configuration
public class OpenApiConfiguration {

    @Value("${application.version}")
    private String version;

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
        final OpenAPI api = new OpenAPI()
            .info(new Info()
                .title("Opertus Mundi API Gateway")
                .version(this.version)
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

        api.getInfo().addExtension(
            "x-logo",
            LogoExtension.builder()
                .altText("OpertusMundi")
                .backgroundColor("#FAFAFA")
                .url("./assets/img/logo-black.svg")
                .build()
        );

        api.addExtension("x-tagGroups", new TagGroup[] {
            new TagGroup("Account", Arrays.asList("Account", "Profile", "Configuration", "File System")),
            new TagGroup("Customers", Arrays.asList("Consumer Registration", "Provider Registration" )),
            new TagGroup("Orders", Arrays.asList("Catalogue", "Cart")),
            new TagGroup("Assets", Arrays.asList("Provider Assets", "Provider Drafts", "Rating")),
            new TagGroup("Messages", Arrays.asList("Message", "Notification")),
        });

        return api;
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

    @Bean
    public OpenApiCustomiser orderedOperationsOpenApiCustomiser() {
        return openapi -> {
            final Paths paths = new Paths();

            openapi.getPaths().entrySet().stream()
                .sorted(Comparator.comparing(p -> this.extractOperationId(p.getValue())))
                .forEachOrdered(p -> paths.addPathItem(p.getKey(), p.getValue()));

            openapi.setPaths(paths);
        };
    }

    private String extractOperationId(PathItem p) {
        if (p.getGet() != null) {
            return p.getGet().getOperationId();
        } else if (p.getPost() != null) {
            return p.getPost().getOperationId();
        } else if (p.getPut() != null) {
            return p.getPut().getOperationId();
        } else if (p.getDelete() != null) {
            return p.getDelete().getOperationId();
        }
        return null;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    public static class LogoExtension {
        String altText;
        String backgroundColor;
        String url;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TagGroup {
        String       name;
        List<String> tags;
    }

}
