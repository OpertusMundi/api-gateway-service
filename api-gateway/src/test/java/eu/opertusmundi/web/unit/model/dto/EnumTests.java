package eu.opertusmundi.web.unit.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import eu.opertusmundi.common.model.EnumExternalDataProviderRole;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.EnumVendorRole;
import eu.opertusmundi.common.model.pricing.EnumExternalDataProviderPricingModel;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;

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

    @Test
    void enumPricingModelMustIncludeEnumExternalDataProviderPricingModelValues() throws Exception {
        final List<String> models         = Arrays.stream(EnumPricingModel.values())
            .map(EnumPricingModel::name)
            .collect(Collectors.toList());
        final List<String> externalModels = Arrays.stream(EnumExternalDataProviderPricingModel.values())
            .map(EnumExternalDataProviderPricingModel::name)
            .collect(Collectors.toList());

        assertThat(models).containsAll(externalModels);
    }

}
