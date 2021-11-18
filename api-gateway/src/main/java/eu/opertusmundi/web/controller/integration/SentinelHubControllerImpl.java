package eu.opertusmundi.web.controller.integration;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto;
import eu.opertusmundi.common.model.sinergise.client.ClientCatalogueQueryDto;
import eu.opertusmundi.common.service.integration.SentinelHubService;
import eu.opertusmundi.web.controller.action.BaseController;

@RestController
@ConditionalOnProperty(name = "opertusmundi.sentinel-hub.enabled")
public class SentinelHubControllerImpl extends BaseController implements SentinelHubController {

    @Autowired
    private SentinelHubService sentinelHub;

    @Override
    public RestResponse<?> search(@Valid ClientCatalogueQueryDto query, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final CatalogueResponseDto result = sentinelHub.search(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> getSubscriptionPlans() {
        final SubscriptionPlanDto plan = SubscriptionPlanDto.builder()
            .title("Free")
            .features(Arrays.<String[]>asList(
                new String[] {"Download analytical data with EO Browser", "Community support","Non-commercial use"}
            ))
            .license("Creative Commons Attribution-NonCommercial 4.0 International License")
            .build();

        final List<SubscriptionPlanDto> result = Arrays.asList(plan);

        return RestResponse.result(result);
    }
}