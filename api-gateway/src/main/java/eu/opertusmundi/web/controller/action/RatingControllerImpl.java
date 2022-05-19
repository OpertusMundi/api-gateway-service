package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.rating.AssetRatingCommandDto;
import eu.opertusmundi.common.model.rating.ProviderRatingCommandDto;
import eu.opertusmundi.common.model.rating.RatingDto;
import eu.opertusmundi.common.service.RatingService;
import eu.opertusmundi.web.validation.AssetRatingValidator;
import eu.opertusmundi.web.validation.ProviderRatingValidator;

@RestController
public class RatingControllerImpl extends BaseController implements RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private AssetRatingValidator assetRatingValidator;

    @Autowired
    private ProviderRatingValidator providerRatingValidator;

    @Override
    public RestResponse<List<RatingDto>> getAssetRatings(String id) {
        final List<RatingDto> result = this.ratingService.getAssetRatings(id);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<List<RatingDto>> getProviderRatings(UUID id) {
        final List<RatingDto> result = this.ratingService.getProviderRatings(id);

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse addAssetRating(String id, AssetRatingCommandDto command, BindingResult validationResult) {
        command.setAccount(this.currentUserKey());
        command.setAsset(id);

        this.assetRatingValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        this.ratingService.addAssetRating(command);

        return RestResponse.success();
    }

    @Override
    public BaseResponse addProviderRating(UUID id, ProviderRatingCommandDto command, BindingResult validationResult) {
        command.setAccount(this.currentUserKey());
        command.setProvider(id);

        this.providerRatingValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        this.ratingService.addProviderRating(command);

        return RestResponse.success();
    }

}