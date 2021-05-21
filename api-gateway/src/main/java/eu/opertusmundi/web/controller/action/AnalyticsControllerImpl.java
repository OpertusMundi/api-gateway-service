package eu.opertusmundi.web.controller.action;

import java.util.ArrayList;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.SalesQuery;
import eu.opertusmundi.common.service.DataAnalysisService;

@RestController
public class AnalyticsControllerImpl extends BaseController implements AnalyticsController {

    @Autowired
    private DataAnalysisService analysisService;

    @Override
    public RestResponse<?> executeSalesQuery(@Valid SalesQuery query, BindingResult validationResult) {
        // Override publisher key
        if (query.getPublishers() == null) {
            query.setPublishers(new ArrayList<>());
        }
        query.getPublishers().clear();
        query.getPublishers().add(this.currentUserKey());

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.execute(query);

        return RestResponse.result(result);
    }

}
