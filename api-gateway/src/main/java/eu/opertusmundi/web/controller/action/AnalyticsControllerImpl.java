package eu.opertusmundi.web.controller.action;

import java.util.ArrayList;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.BaseQuery;
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
        // View parent account (vendor) analytics
        query.getPublishers().add(this.currentUserParentKey());

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.execute(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> executeAssetQuery(@Valid AssetViewQuery query, BindingResult validationResult) {
        // Override publisher key
        if (query.getPublishers() == null) {
            query.setPublishers(new ArrayList<>());
        }
        query.getPublishers().clear();
        // View parent account (vendor) analytics
        query.getPublishers().add(this.currentUserParentKey());

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.execute(query);

        return RestResponse.result(result);
    }
    
    @Override
    public RestResponse<?> executeCoverageQuery(@Valid BaseQuery query, BindingResult validationResult) {
        // Override publisher key
        if (query.getPublishers() == null) {
            query.setPublishers(new ArrayList<>());
        }
        query.getPublishers().clear();
        // View parent account (vendor) analytics
        query.getPublishers().add(this.currentUserParentKey());

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.executeCoverage(query);

        return RestResponse.result(result);
    }
    
    @Override
    public RestResponse<?> executePriceQuery(@Valid BaseQuery query, BindingResult validationResult) {
        // Override publisher key
        if (query.getPublishers() == null) {
            query.setPublishers(new ArrayList<>());
        }
        query.getPublishers().clear();
        // View parent account (vendor) analytics
        query.getPublishers().add(this.currentUserParentKey());

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.executeTotalPrice(query);

        return RestResponse.result(result);
    }


}
