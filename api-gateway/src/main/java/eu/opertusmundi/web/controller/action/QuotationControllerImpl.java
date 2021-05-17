package eu.opertusmundi.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationCommandDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.QuotationService;

@RestController
public class QuotationControllerImpl extends BaseController implements QuotationController {

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private QuotationService quotationService;

    @Override
    public RestResponse<?> createQuotation(QuotationCommandDto command, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final CatalogueItemDetailsDto  asset     = catalogueService.findOne(null, command.getAssetId(), null, false);
            final EffectivePricingModelDto quotation = quotationService.createQuotation(
                asset, command.getPricingModelKey(), command.getParameters()
            );

            return RestResponse.result(quotation);
        } catch (final QuotationException ex) {
            return RestResponse.failure(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            return RestResponse.failure();
        }
    }

}
