package eu.opertusmundi.web.config;

import java.util.ArrayList;
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

import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
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

@Configuration
public class OpenApiConfiguration {

    @Value("${application.version}")
    private String version;

    @Value("${springdoc.api-docs.server:http://localhost:8080}")
    private String serverUrl;

    @Value("${server.servlet.session.cookie.name}")
    private String sessionCookieName;

    @Value("${opertusmundi.sentinel-hub.enabled:false}")
    private boolean sentinelHubIntegration;

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
                .addSecuritySchemes(
                    "jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .in(In.HEADER)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token authentication")
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

        final List<TagGroup> tagGroups = new ArrayList<>();

        tagGroups.addAll(Arrays.asList(new TagGroup[] {
            new TagGroup("System", Arrays.asList(
                EndpointTags.Configuration
            )),
            new TagGroup("Account", Arrays.asList(
                EndpointTags.Account,
                EndpointTags.VendorAccount,
                EndpointTags.Profile,
                EndpointTags.FileSystem,
                EndpointTags.AccountClient
            )),
            new TagGroup("KYC", Arrays.asList(
                EndpointTags.KycDocument,
                EndpointTags.UboDeclaration
            )),
            new TagGroup("Consumers", Arrays.asList(
                EndpointTags.ConsumerRegistration,
                EndpointTags.Favorites,
                EndpointTags.Notebooks,
                EndpointTags.OrderConsumer,
                EndpointTags.PayInConsumer,
                EndpointTags.ContractConsumer,
                EndpointTags.ConsumerAssets,
                EndpointTags.ConsumerServiceBilling
            )),
            new TagGroup("Providers", Arrays.asList(
                EndpointTags.ProviderRegistration,
                EndpointTags.OrderProvider,
                EndpointTags.PayInProvider,
                EndpointTags.PayOutProvider,
                EndpointTags.ContractProvider,
                EndpointTags.ContractProviderTemplates,
                EndpointTags.ProviderAssets,
                EndpointTags.ProviderServiceBilling,
                EndpointTags.Analytics
            )),
            new TagGroup("Helpdesk", Arrays.asList(
                EndpointTags.DraftReview
            )),
            new TagGroup("User Services", Arrays.asList(
                EndpointTags.UserServices
            )),
            new TagGroup("Assets", Arrays.asList(
                EndpointTags.Draft,
                EndpointTags.Samples
            )),
            new TagGroup("Purchase", Arrays.asList(
                EndpointTags.Catalogue,
                EndpointTags.Cart,
                EndpointTags.Quotation
            )),
            new TagGroup("Messages", Arrays.asList(
                EndpointTags.Message,
                EndpointTags.Notification
            )),
            new TagGroup("Misc", Arrays.asList(
                EndpointTags.Rating,
                EndpointTags.SpatialData
            )),
            new TagGroup("API", Arrays.asList(
                EndpointTags.API_Profile,
                EndpointTags.API_FileSystem,
                EndpointTags.API_ConsumerAssets,
                EndpointTags.API_UserServices
            )),
        }));

        if(this.sentinelHubIntegration) {
            tagGroups.add(
                new TagGroup("Integration", Arrays.asList(
                    EndpointTags.SentinelHub
                ))
            );
        }

        api.addExtension("x-tagGroups", tagGroups);

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

            tags.add(0, profileTag.get());
            tags.add(0, accountTag.get());

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
    public static class LogoExtension {
        String altText;
        String backgroundColor;
        String url;
    }

    @AllArgsConstructor
    @Getter
    public static class TagGroup {
        String       name;
        List<String> tags;
    }

}
