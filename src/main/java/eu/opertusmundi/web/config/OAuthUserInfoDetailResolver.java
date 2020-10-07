package eu.opertusmundi.web.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "opertus-mundi.auth")
public class OAuthUserInfoDetailResolver {

    private List<String>       nameAliases;

    private List<String>       emailAliases;

    private List<String>       localeAliases;

    private List<String>       imageAliases;

    public static final String NAME_PROPERTY   = "name";

    public static final String EMAIL_PROPERTY  = "email";

    public static final String LOCALE_PROPERTY = "locale";

    public static final String IMAGE_PROPERTY  = "image";

    public List<String> getNameAliases() {
        return this.nameAliases;
    }

    public void setNameAliases(List<String> nameAliases) {
        this.nameAliases = nameAliases;
    }

    public List<String> getEmailAliases() {
        return this.emailAliases;
    }

    public void setEmailAliases(List<String> emailAliases) {
        this.emailAliases = emailAliases;
    }

    public List<String> getLocaleAliases() {
        return this.localeAliases;
    }

    public void setLocaleAliases(List<String> localeAliases) {
        this.localeAliases = localeAliases;
    }

    public List<String> getImageAliases() {
        return this.imageAliases;
    }

    public void setImageAliases(List<String> imageAliases) {
        this.imageAliases = imageAliases;
    }

    public String resolve(String key) {
        if (this.nameAliases.stream().anyMatch(alias -> key.trim().equals(alias))) {
            return NAME_PROPERTY;
        }
        if (this.emailAliases.stream().anyMatch(alias -> key.trim().equals(alias))) {
            return EMAIL_PROPERTY;
        }
        if (this.localeAliases.stream().anyMatch(alias -> key.trim().equals(alias))) {
            return LOCALE_PROPERTY;
        }
        if (this.imageAliases.stream().anyMatch(alias -> key.trim().equals(alias))) {
            return IMAGE_PROPERTY;
        }

        return null;
    }

}
