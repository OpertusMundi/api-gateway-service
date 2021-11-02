package eu.opertusmundi.web.utils;

import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.model.account.PlatformAccountCommandDto;

public class AccountCommandFactory {

    public static AccountProfileCommandDto.AccountProfileCommandDtoBuilder profile() {
        return AccountProfileCommandDto.builder()
            .firstName("Demo")
            .lastName("User")
            .locale("en")
            .mobile("+306900000000")
            .phone("+302100000000");
    }

    public static PlatformAccountCommandDto.PlatformAccountCommandDtoBuilder user() {
        return AccountCommandFactory.user("user@opertusmundi.eu", "password");
    }

    public static PlatformAccountCommandDto.PlatformAccountCommandDtoBuilder user(String email) {
        return AccountCommandFactory.user(email, "password");
    }

    public static PlatformAccountCommandDto.PlatformAccountCommandDtoBuilder user(String email, String password) {
        return PlatformAccountCommandDto.builder()
            .active(true)
            .blocked(false)
            .email(email)
            .password(password)
            .profile(AccountCommandFactory.profile().build());
    }

}
