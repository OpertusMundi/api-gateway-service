package eu.opertusmundi.web.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;

public class ContractEndpointTypes {

    public static class MasterContractCollection extends RestResponse<PageResultDto<MasterContractDto>> {

    }

    public static class MasterContract extends RestResponse<MasterContractDto> {

    }

    public static class ProviderDraftContractTemplateCollection extends RestResponse<List<ProviderTemplateContractDto>> {

    }

    public static class ProviderDraftContractTemplate extends RestResponse<ProviderTemplateContractDto> {

    }

    public static class ProviderContractTemplateCollection extends RestResponse<List<ProviderTemplateContractDto>> {

    }

    public static class ProviderContractTemplate extends RestResponse<ProviderTemplateContractDto> {

    }
}
