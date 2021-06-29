package eu.opertusmundi.web.controller.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractQuery;
import eu.opertusmundi.common.service.contract.ProviderTemplateContractService;

@RestController
public class ProviderContractControllerImpl extends BaseController implements ProviderContractController {

    @Autowired
    private ProviderTemplateContractService templateContractService;

    @Override
    public RestResponse<?> findAll() {
        final ProviderTemplateContractQuery query = ProviderTemplateContractQuery.builder()
            .active(true)
            .providerKey(this.currentUserKey())
            .build();

        final List<ProviderTemplateContractDto> result = templateContractService.findAll(query);

        return RestResponse.result(result);
    }

}
