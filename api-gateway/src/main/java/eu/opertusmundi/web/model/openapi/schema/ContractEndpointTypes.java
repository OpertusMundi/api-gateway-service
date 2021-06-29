package eu.opertusmundi.web.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;

public class ContractEndpointTypes {

    public static class ProviderContractTemplateCollection extends RestResponse<List<ProviderTemplateContractDto>> {

    }
}
