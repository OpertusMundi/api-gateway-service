package eu.opertusmundi.web.unit.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.opertusmundi.common.model.EnumExternalDataProviderRole;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.EnumVendorRole;

@JsonTest
@ExtendWith(SpringExtension.class)
public class EnumTests {

    @Test
    void enumRoleMustIncludeEnumVendorRoleValues() throws Exception {
        final List<String> roles       = Arrays.stream(EnumRole.values())
            .map(EnumRole::name)
            .collect(Collectors.toList());
        final List<String> vendorRoles = Arrays.stream(EnumVendorRole.values())
            .map(EnumVendorRole::name)
            .collect(Collectors.toList());

        assertThat(roles).containsAll(vendorRoles);
    }

    @Test
    void enumRoleMustIncludeEnumExternalDataProviderRoleValues() throws Exception {
        final List<String> roles         = Arrays.stream(EnumRole.values())
            .map(EnumRole::name)
            .collect(Collectors.toList());
        final List<String> externalRoles = Arrays.stream(EnumExternalDataProviderRole.values())
            .map(EnumExternalDataProviderRole::name)
            .collect(Collectors.toList());

        assertThat(roles).containsAll(externalRoles);
    }

}
