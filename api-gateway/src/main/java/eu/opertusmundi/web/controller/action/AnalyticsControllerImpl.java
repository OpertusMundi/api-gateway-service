package eu.opertusmundi.web.controller.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.analytics.AssetCountQuery;
import eu.opertusmundi.common.model.analytics.AssetTotalValueQuery;
import eu.opertusmundi.common.model.analytics.AssetTypeEarningsQuery;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.BaseQuery;
import eu.opertusmundi.common.model.analytics.CoverageQuery;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.GoogleAnalyticsQuery;
import eu.opertusmundi.common.model.analytics.SalesQuery;
import eu.opertusmundi.common.model.analytics.SubscribersQuery;
import eu.opertusmundi.common.model.analytics.VendorCountQuery;
import eu.opertusmundi.common.service.DataAnalysisService;
import eu.opertusmundi.common.service.analytics.GoogleAnalyticsService;

@RestController
public class AnalyticsControllerImpl extends BaseController implements AnalyticsController {

    @Autowired
    private DataAnalysisService analysisService;

    @Autowired(required = false)
    private GoogleAnalyticsService googleAnalyticsService;

    @Override
    public RestResponse<?> executeSalesQuery(SalesQuery query, BindingResult validationResult) {
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
    public RestResponse<?> executeAssetQuery(AssetViewQuery query, BindingResult validationResult) {
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
    public RestResponse<?> executeCoverageQuery(CoverageQuery query, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.executeCoverage(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> executeTotalAssetValueQuery(AssetTotalValueQuery query, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.executeTotalAssetValue(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> executeAssetCountQuery(AssetCountQuery query, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.executeAssetCount(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> executeFindPopularAssetViewsAndSearches(AssetViewQuery query, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final List<ImmutablePair<String, Integer>> result = this.analysisService.executePopularAssetViewsAndSearches(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> executeFindPopularTerms(BaseQuery query, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }        
    	
    	final List<ImmutablePair<String, Integer>> result = this.analysisService.executePopularTerms(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> executeVendorCountQuery(VendorCountQuery query, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = analysisService.executeVendorCount(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> executeSubscribersQuery(SubscribersQuery query, BindingResult validationResult) {
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
    public RestResponse<?> executeGoogleAnalytics(GoogleAnalyticsQuery query, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final DataSeries<?> result = googleAnalyticsService == null ? new DataSeries<BigDecimal>() : googleAnalyticsService.execute(query);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> executeEarningsPerAssetType(AssetTypeEarningsQuery query, BindingResult validationResult) {
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

}
