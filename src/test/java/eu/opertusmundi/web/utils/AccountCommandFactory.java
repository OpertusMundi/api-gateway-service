package eu.opertusmundi.web.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;

public class AccountCommandFactory {

    public static AccountProfileCommandDto.AccountProfileCommandDtoBuilder profile() {
        return AccountProfileCommandDto.builder()
            .firstName("Demo")
            .lastName("User")
            .locale("en")
            .mobile("+306900000000")
            .phone("+302100000000");
    }

    public static AccountCommandDto.AccountCommandDtoBuilder user() {
        return AccountCommandFactory.user("user@opertusmundi.eu", "password");
    }

    public static AccountCommandDto.AccountCommandDtoBuilder user(String email) {
        return AccountCommandFactory.user(email, "password");
    }

    public static AccountCommandDto.AccountCommandDtoBuilder user(String email, String password) {
        final EnumRole[]    roleArray = {EnumRole.ROLE_USER};
        final Set<EnumRole> roleSet   = new HashSet<EnumRole>(Arrays.asList(roleArray));

        return AccountCommandDto.builder()
            .active(true)
            .blocked(false)
            .email(email)
            .password(password)
            .profile(AccountCommandFactory.profile().build())
            .roles(roleSet);
    }

}
